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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreHotelRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HotelRepository {

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
                        price = (roomData?.get("price") as? Double) ?: 0.0,
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
                        price = (roomData?.get("price") as? Double) ?: 0.0,
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
            minPrice = (data["minPrice"] as? Number)?.toDouble(),
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
