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
import kotlin.collections.mapOf

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
                    roomDoc.toObject(Room::class.java)?.copy(id = roomDoc.id)
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

    override suspend fun createHotel(hotel: Hotel): String {
        try {
            Log.d("FirestoreHotelRepository", "Creating hotel in Firestore")
            val data = hotelToMap(hotel)
            val docRef = firestore.collection("hotels")
                .add(data)
                .await()
            Log.d("FirestoreHotelRepository", "Created hotel with id=${docRef.id}")
            return docRef.id
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreHotelRepository", "Firestore error creating hotel: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreHotelRepository", "Unexpected error creating hotel: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateHotel(hotel: Hotel) {
        try {
            Log.d("FirestoreHotelRepository", "Updating hotel id=${hotel.id}")
            val data = hotelToMap(hotel)
            firestore.collection("hotels")
                .document(hotel.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d("FirestoreHotelRepository", "Updated hotel id=${hotel.id}")
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreHotelRepository", "Firestore error updating hotel id=${hotel.id}: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreHotelRepository", "Unexpected error updating hotel id=${hotel.id}: ${e.message}", e)
            throw e
        }
    }
    private fun hotelToMap(hotel: Hotel): Map<String, Any?> {
        val coordMap = hotel.coordinate.let { mapOf("lat" to it.latitude, "lng" to it.longitude) }

        val policies = hotel.policy.map { mapOf("title" to it.title, "content" to it.content) }
        val images = hotel.imageUrl
        val languages = hotel.language
        val features = hotel.feature

        return mapOf(
            "name" to hotel.name,
            "description" to hotel.description,
            "propertyType" to hotel.propertyType,
            "formattedAddress" to hotel.formattedAddress,
            "country" to hotel.country,
            "city" to hotel.city,
            "coordinate" to coordMap,
            "imageUrl" to images,
            "policy" to policies,
            "language" to languages,
            "feature" to features,
            "minPrice" to hotel.minPrice,
            "rating" to hotel.rating,
            "numberOfReviews" to hotel.numberOfReviews
        )
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
                    roomDoc.toObject(Room::class.java)?.copy(id = roomDoc.id)
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

    override suspend fun reserveRoomUnits(roomId: String, count: Int): Boolean {
        return try {
            firestore.runTransaction { txn ->
                val roomRef = firestore.collection("rooms").document(roomId)
                val snapshot = txn.get(roomRef)
                if (!snapshot.exists()) throw IllegalStateException("Room not found")
                val current = (snapshot.getLong("availableCount") ?: 0L).toInt()
                if (count <= 0) throw IllegalArgumentException("Reserve count must be > 0")
                if (current < count) throw IllegalStateException("Not enough availability")
                val newCount = current - count
                val updates = mutableMapOf<String, Any>("availableCount" to newCount)
                if (newCount == 0) updates["isAvailable"] = false
                txn.update(roomRef, updates)
                true
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun releaseRoomUnits(roomId: String, count: Int): Boolean {
        return try {
            firestore.runTransaction { txn ->
                val roomRef = firestore.collection("rooms").document(roomId)
                val snapshot = txn.get(roomRef)
                if (!snapshot.exists()) throw IllegalStateException("Room not found")
                val current = (snapshot.getLong("availableCount") ?: 0L).toInt()
                if (count <= 0) throw IllegalArgumentException("Release count must be > 0")
                val newCount = current + count
                val updates = mutableMapOf<String, Any>("availableCount" to newCount)
                if (newCount > 0) updates["isAvailable"] = true
                txn.update(roomRef, updates)
                true
            }.await()
        } catch (e: Exception) {
            false
        }
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

    data class RoomGalleryImport(
        val exteriorView: List<String> = emptyList(),
        val facilities: List<String> = emptyList(),
        val dining: List<String> = emptyList(),
        val thisRoom: List<String> = emptyList()
    )

    data class RoomImport(
        val id: String? = null,
        val type: String = "",
        val price: Double = 0.0,
        val imageUrl: String? = null,
        val isAvailable: Boolean = true,
        val capacity: Int = 0,
        val availableCount: Int = 0,
        val facilities: List<String> = emptyList(),
        val detail: RoomDetail? = null,
        val gallery: RoomGalleryImport = RoomGalleryImport()
    )

    suspend fun importRooms(hotelId: String, rooms: List<RoomImport>, merge: Boolean = true): Int {
        var written = 0
        for (r in rooms) {
            val docData = mutableMapOf<String, Any?>()
            docData["hotelId"] = hotelId
            docData["type"] = r.type
            docData["price"] = r.price
            docData["imageUrl"] = r.imageUrl ?: ""
            docData["isAvailable"] = r.isAvailable
            docData["capacity"] = r.capacity
            docData["availableCount"] = r.availableCount
            docData["facilities"] = r.facilities
            r.detail?.let { d ->
                val dd = mutableMapOf<String, Any>()
                d.name?.let { dd["name"] = it }
                d.size?.let { dd["size"] = it }
                d.view?.let { dd["view"] = it }
                docData["detail"] = dd
            }
            docData["gallery"] = mapOf(
                "exteriorView" to r.gallery.exteriorView,
                "facilities" to r.gallery.facilities,
                "dining" to r.gallery.dining,
                "thisRoom" to r.gallery.thisRoom
            )

            val collection = firestore.collection("rooms")
            if (r.id.isNullOrBlank()) {
                val ref = collection.add(docData).await()
                if (merge) ref.set(docData, SetOptions.merge()).await() else ref.set(docData).await()
            } else {
                val ref = collection.document(r.id)
                if (merge) ref.set(docData, SetOptions.merge()).await() else ref.set(docData).await()
            }
            written++
        }
        return written
    }

    override suspend fun updateHotelAggregation(hotelId: String, rating: Double, numberOfReviews: Int): Boolean {
        return try {
            val updates = mapOf(
                "rating" to rating,
                "numberOfReviews" to numberOfReviews
            )
            firestore.collection("hotels")
                .document(hotelId)
                .set(updates, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreHotelRepository", "Failed to update hotel aggregation for $hotelId: ${e.message}", e)
            false
        }
    }
}
