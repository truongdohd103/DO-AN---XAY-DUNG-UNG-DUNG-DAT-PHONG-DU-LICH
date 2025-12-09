package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Coordinate
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Policy
import com.example.chillstay.domain.model.PropertyType
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomDetail
import com.example.chillstay.domain.repository.HotelRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreHotelRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HotelRepository {

    data class HotelDataConsistencyReport(
        val hotelsChecked: Int,
        val hotelsFixed: Int,
        val roomsChecked: Int,
        val roomsFixed: Int,
        val roomsDeleted: Int,
        val bookingsChecked: Int,
        val bookingsUpdated: Int,
        val issues: List<String>
    )

    override suspend fun getHotels(): List<Hotel> {
        return try {
            Log.d("FirestoreHotelRepository", "Attempting to fetch hotels from Firestore")
            val snapshot = firestore.collection("hotels")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()
            
            Log.d("FirestoreHotelRepository", "Successfully fetched ${snapshot.documents.size} hotels")
            snapshot.documents.mapNotNull { document ->
                val hotel = mapHotelDocument(document)
                
                // Load rooms for this hotel
                val roomsSnapshot = firestore.collection("rooms")
                    .whereEqualTo("hotelId", document.id)
                    .get()
                    .await()
                
                val rooms = roomsSnapshot.documents.mapNotNull { roomDoc ->
                    val roomData = roomDoc.data
                    Room(
                        id = roomDoc.id,
                        hotelId = roomData?.get("hotelId") as? String ?: "",
                        type = roomData?.get("type") as? String ?: "",
                        price = (roomData?.get("price") as? Number)?.toDouble()
                            ?: (roomData?.get("price") as? String)?.toDoubleOrNull()
                            ?: 0.0,
                        imageUrl = roomData?.get("imageUrl") as? String ?: "",
                        isAvailable = roomData?.get("isAvailable") as? Boolean ?: true,
                        capacity = (roomData?.get("capacity") as? Long)?.toInt() ?: 0,
                        detail = roomData?.get("detail")?.let { detailData ->
                            if (detailData is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val detailMap = detailData as Map<String, Any>
                                RoomDetail(
                                    name = detailMap["name"] as? String ?: "",
                                    size = (detailMap["size"] as? Double) ?: 0.0,
                                    view = detailMap["view"] as? String ?: ""
                                )
                            } else null
                        }
                    )
                }

                hotel?.copy(rooms = rooms)
            }
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                    Log.w("FirestoreHotelRepository", "Index not found for hotels query. Please create index in Firebase Console: ${e.message}")
                    Log.w("FirestoreHotelRepository", "Index required: collection=hotels, fields=rating(desc)")
                    emptyList()
                }
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w("FirestoreHotelRepository", "Permission denied accessing hotels: ${e.message}")
                    emptyList()
                }
                else -> {
                    Log.e("FirestoreHotelRepository", "Firestore error fetching hotels: ${e.message}", e)
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreHotelRepository", "Unexpected error fetching hotels: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun searchHotels(
        query: String,
        country: String?,
        city: String?,
        minRating: Double?,
        maxPrice: Double?
    ): List<Hotel> {
        return try {
            var firestoreQuery: Query = firestore.collection("hotels")
            
            // Apply filters
            country?.trim()?.let { firestoreQuery = firestoreQuery.whereEqualTo("country", it) }
            city?.trim()?.let { firestoreQuery = firestoreQuery.whereEqualTo("city", it) }
            // Avoid inequality filters in Firestore to prevent composite index requirements
            
            val snapshot = firestoreQuery.get().await()
            Log.d("FirestoreHotelRepository", "searchHotels fetched ${snapshot.documents.size} docs from Firestore")
            val mapped = snapshot.documents.mapNotNull { document ->
                mapHotelDocument(document)
            }
            Log.d("FirestoreHotelRepository", "searchHotels mapped ${mapped.size} hotels")

            val q = query.trim()
            val filtered = mapped
                .filter { hotel ->
                    q.isEmpty() ||
                    hotel.name.contains(q, ignoreCase = true) ||
                    hotel.city.contains(q, ignoreCase = true) ||
                    hotel.country.contains(q, ignoreCase = true) ||
                    hotel.description.contains(q, ignoreCase = true)
                }

            Log.d(
                "FirestoreHotelRepository",
                "searchHotels mapped names=${mapped.joinToString(limit = 10, truncated = "...") { it.name }}"
            )
            Log.d("FirestoreHotelRepository", "searchHotels returning ${filtered.size} hotels after filters")
            filtered
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                    Log.w("FirestoreHotelRepository", "Index not found. Consider using client-side filtering or create composite index if needed: ${e.message}")
                    emptyList()
                }
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w("FirestoreHotelRepository", "Permission denied searching hotels: ${e.message}")
                    emptyList()
                }
                else -> {
                    Log.e("FirestoreHotelRepository", "Firestore error searching hotels: ${e.message}", e)
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreHotelRepository", "Unexpected error searching hotels: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getHotelById(id: String): Hotel? {
        return try {
            Log.d("FirestoreHotelRepository", "Fetching hotel from Firestore, hotelId=$id")
            val document = firestore.collection("hotels")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                Log.d(
                    "FirestoreHotelRepository",
                    "Firestore returned hotel document for id=$id. Converting to model..."
                )
                val hotel = mapHotelDocument(document)
                if (hotel != null) {
                    Log.d(
                        "FirestoreHotelRepository",
                        "Converted hotelId=${hotel.id}, imageCount=${hotel.imageUrl.size}, images=${hotel.imageUrl}"
                    )
                } else {
                    Log.w(
                        "FirestoreHotelRepository",
                        "Document for hotelId=$id exists but failed to map to Hotel model"
                    )
                }
                
                // Load rooms for this hotel
                val roomsSnapshot = firestore.collection("rooms")
                    .whereEqualTo("hotelId", id)
                    .get()
                    .await()
                
                val rooms = roomsSnapshot.documents.mapNotNull { roomDoc ->
                    val roomData = roomDoc.data
                    Room(
                        id = roomDoc.id,
                        hotelId = roomData?.get("hotelId") as? String ?: "",
                        type = roomData?.get("type") as? String ?: "",
                        price = (roomData?.get("price") as? Number)?.toDouble()
                            ?: (roomData?.get("price") as? String)?.toDoubleOrNull()
                            ?: 0.0,
                        imageUrl = roomData?.get("imageUrl") as? String ?: "",
                        isAvailable = roomData?.get("isAvailable") as? Boolean ?: true,
                        capacity = (roomData?.get("capacity") as? Long)?.toInt() ?: 0,
                        detail = roomData?.get("detail")?.let { detailData ->
                            if (detailData is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val detailMap = detailData as Map<String, Any>
                                RoomDetail(
                                    name = detailMap["name"] as? String ?: "",
                                    size = (detailMap["size"] as? Double) ?: 0.0,
                                    view = detailMap["view"] as? String ?: ""
                                )
                            } else null
                        }
                    )
                }
                val hotelWithRooms = hotel?.copy(rooms = rooms)
                if (hotelWithRooms != null) {
                    Log.d(
                        "FirestoreHotelRepository",
                        "Returning hotelId=${hotelWithRooms.id} with ${hotelWithRooms.rooms.size} rooms"
                    )
                }
                hotelWithRooms
            } else {
                Log.w("FirestoreHotelRepository", "Hotel document not found for id=$id")
                null
            }
        } catch (e: Exception) {
            Log.e(
                "FirestoreHotelRepository",
                "Error fetching hotelId=$id from Firestore: ${e.message}",
                e
            )
            null
        }
    }

    override suspend fun getHotelsByCity(city: String): List<Hotel> {
        return try {
            val snapshot = firestore.collection("hotels")
                .whereEqualTo("city", city)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                mapHotelDocument(document)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getHotelRooms(
        hotelId: String,
        checkIn: String?,
        checkOut: String?,
        guests: Int?
    ): List<Room> {
        return try {
            var query = firestore.collection("rooms")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("isAvailable", true)
            
            guests?.let { query = query.whereGreaterThanOrEqualTo("capacity", it) }
            
            val snapshot = query.get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Room::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getRoomById(roomId: String): Room? {
        return try {
            val document = firestore.collection("rooms")
                .document(roomId)
                .get()
                .await()
            if (document.exists()) {
                document.toObject(Room::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkAndFixHotelData(): HotelDataConsistencyReport {
        val issues = mutableListOf<String>()

        val hotelsSnapshot = firestore.collection("hotels").get().await()
        val hotelDocs = hotelsSnapshot.documents
        var hotelsFixed = 0

        for (doc in hotelDocs) {
            val data = doc.data ?: continue
            val updates = mutableMapOf<String, Any?>()

            val name = data["name"]
            if (name !is String || name.isBlank()) {
                issues.add("hotel ${doc.id} thiếu hoặc name không hợp lệ")
            }

            when (val imageVal = data["imageUrl"]) {
                is String -> {
                    updates["imageUrl"] = listOf(imageVal)
                }
                is List<*> -> {
                    val normalized = imageVal.mapNotNull { it as? String }
                    if (normalized.size != imageVal.size) {
                        updates["imageUrl"] = normalized
                    }
                }
                null -> updates["imageUrl"] = emptyList<String>()
            }

            when (val langVal = data["language"]) {
                is String -> updates["language"] = listOf(langVal)
                is List<*> -> {
                    val normalized = langVal.mapNotNull { it as? String }
                    if (normalized.size != langVal.size) updates["language"] = normalized
                }
                null -> updates["language"] = emptyList<String>()
            }

            when (val minPrice = data["minPrice"]) {
                is Number -> {}
                is String -> minPrice.toDoubleOrNull()?.let { updates["minPrice"] = it }
                else -> {}
            }

            when (val reviews = data["numberOfReviews"]) {
                is Number -> {}
                is String -> reviews.toIntOrNull()?.let { updates["numberOfReviews"] = it }
                else -> {}
            }

            val propertyType = (data["propertyType"] as? String)?.uppercase()
            if (propertyType == null || (propertyType != "HOTEL" && propertyType != "RESORT")) {
                updates["propertyType"] = "HOTEL"
            }

            when (val rating = data["rating"]) {
                is Number -> {}
                is String -> rating.toDoubleOrNull()?.let { updates["rating"] = it }
                else -> updates["rating"] = 0.0
            }

            val policyVal = data["policy"]
            when (policyVal) {
                is Map<*, *> -> {
                    val title = policyVal["title"] as? String ?: ""
                    val content = policyVal["content"] as? String ?: ""
                    updates["policy"] = listOf(mapOf("title" to title, "content" to content))
                }
                is List<*> -> {
                    val normalized = policyVal.mapNotNull { entry ->
                        val m = entry as? Map<*, *>
                        val t = m?.get("title") as? String
                        val c = m?.get("content") as? String
                        if (t != null && c != null) mapOf("title" to t, "content" to c) else null
                    }
                    if (normalized.size != policyVal.size) updates["policy"] = normalized
                }
                else -> {}
            }

            if (updates.isNotEmpty()) {
                doc.reference.set(updates, SetOptions.merge()).await()
                hotelsFixed++
            }
        }

        val roomsSnapshot = firestore.collection("rooms").get().await()
        val roomDocs = roomsSnapshot.documents
        var roomsFixed = 0
        var roomsDeleted = 0

        val hotelIds = hotelDocs.map { it.id }.toSet()
        for (room in roomDocs) {
            val data = room.data ?: continue
            val updates = mutableMapOf<String, Any?>()

            val hid = data["hotelId"] as? String
            if (hid.isNullOrBlank() || !hotelIds.contains(hid)) {
                room.reference.delete().await()
                roomsDeleted++
                issues.add("room ${room.id} tham chiếu hotelId không tồn tại")
                continue
            }

            when (val price = data["price"]) {
                is Number -> {}
                is String -> price.toDoubleOrNull()?.let { updates["price"] = it }
                else -> {}
            }

            when (val available = data["isAvailable"]) {
                is Boolean -> {}
                is String -> updates["isAvailable"] = available.equals("true", true)
                null -> updates["isAvailable"] = true
                else -> {}
            }

            when (val cap = data["capacity"]) {
                is Number -> updates["capacity"] = cap.toInt()
                is String -> cap.toIntOrNull()?.let { updates["capacity"] = it }
                else -> {}
            }

            val detail = data["detail"]
            if (detail is Map<*, *>) {
                val name = detail["name"] as? String
                val size = (detail["size"] as? Number)?.toDouble() ?: (detail["size"] as? String)?.toDoubleOrNull()
                val view = detail["view"] as? String
                val normalized = mutableMapOf<String, Any>()
                if (name != null) normalized["name"] = name
                if (size != null) normalized["size"] = size
                if (view != null) normalized["view"] = view
                updates["detail"] = normalized
            }

            if (updates.isNotEmpty()) {
                room.reference.set(updates, SetOptions.merge()).await()
                roomsFixed++
            }
        }

        val bookingsSnapshot = firestore.collection("bookings").get().await()
        var bookingsUpdated = 0
        for (b in bookingsSnapshot.documents) {
            val data = b.data ?: continue
            val hid = data["hotelId"] as? String
            val rid = data["roomId"] as? String
            val update = mutableMapOf<String, Any?>()
            if (hid.isNullOrBlank() || !hotelIds.contains(hid)) {
                update["status"] = "CANCELLED"
                issues.add("booking ${b.id} tham chiếu hotelId không tồn tại")
            }
            if (rid.isNullOrBlank() || roomDocs.none { it.id == rid }) {
                update["status"] = "CANCELLED"
                issues.add("booking ${b.id} tham chiếu roomId không tồn tại")
            }
            if (update.isNotEmpty()) {
                b.reference.set(update, SetOptions.merge()).await()
                bookingsUpdated++
            }
        }

        return HotelDataConsistencyReport(
            hotelsChecked = hotelDocs.size,
            hotelsFixed = hotelsFixed,
            roomsChecked = roomDocs.size,
            roomsFixed = roomsFixed,
            roomsDeleted = roomsDeleted,
            bookingsChecked = bookingsSnapshot.size(),
            bookingsUpdated = bookingsUpdated,
            issues = issues
        )
    }

    private fun mapHotelDocument(document: DocumentSnapshot): Hotel? {
        val data = document.data ?: run {
            Log.w("FirestoreHotelRepository", "Document ${document.id} has no data")
            return null
        }

        val name = data["name"] as? String
        if (name.isNullOrBlank()) {
            Log.w("FirestoreHotelRepository", "Document ${document.id} missing name")
            return null
        }

        val coordinate = when (val coord = data["coordinate"]) {
            is GeoPoint -> Coordinate(coord.latitude, coord.longitude)
            is Map<*, *> -> {
                val lat = (coord["latitude"] as? Number)?.toDouble()
                    ?: (coord["lat"] as? Number)?.toDouble()
                    ?: 0.0
                val lng = (coord["longitude"] as? Number)?.toDouble()
                    ?: (coord["lng"] as? Number)?.toDouble()
                    ?: 0.0
                Coordinate(lat, lng)
            }
            else -> Coordinate()
        }

        return Hotel(
            id = document.id,
            city = data["city"] as? String ?: "",
            coordinate = coordinate,
            country = data["country"] as? String ?: "",
            description = data["description"] as? String ?: "",
            feature = (data["feature"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            formattedAddress = data["formattedAddress"] as? String ?: "",
            imageUrl = (data["imageUrl"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            language = (data["language"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            minPrice = (data["minPrice"] as? Number)?.toDouble()
                ?: (data["minPrice"] as? String)?.toDoubleOrNull(),
            name = name,
            numberOfReviews = (data["numberOfReviews"] as? Number)?.toInt() ?: 0,
            policy = (data["policy"] as? List<*>)?.mapNotNull { entry ->
                val map = entry as? Map<*, *>
                val title = map?.get("title") as? String ?: ""
                val content = map?.get("content") as? String ?: ""
                Policy(title = title, content = content)
            } ?: emptyList(),
            propertyType = when ((data["propertyType"] as? String)?.uppercase()) {
                "RESORT" -> PropertyType.RESORT
                else -> PropertyType.HOTEL
            },
            rating = (data["rating"] as? Number)?.toDouble() ?: 0.0
        )
    }
}
