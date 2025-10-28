package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelDetail
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomDetail
import com.example.chillstay.domain.repository.HotelRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
                val hotel = document.toObject(Hotel::class.java)?.copy(id = document.id)
                
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
                
                // Create a simple HotelDetail
                val hotelDetail = HotelDetail(
                    description = "A beautiful hotel in ${hotel?.city}, ${hotel?.country}",
                    facilities = listOf("WiFi", "Parking", "Restaurant", "Pool"),
                    photoUrls = (1..(hotel?.photoCount ?: 5)).map { "https://placehold.co/600x400" }
                )
                
                hotel?.copy(rooms = rooms, detail = hotelDetail)
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
            country?.let { firestoreQuery = firestoreQuery.whereEqualTo("country", it) }
            city?.let { firestoreQuery = firestoreQuery.whereEqualTo("city", it) }
            minRating?.let { firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("rating", it) }
            maxPrice?.let { firestoreQuery = firestoreQuery.whereLessThanOrEqualTo("priceRange.max", it) }
            
            val snapshot = firestoreQuery.get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Hotel::class.java)?.copy(id = document.id)
            }.filter { hotel ->
                // Text search in memory (for development)
                query.isEmpty() || hotel.name.contains(query, ignoreCase = true) ||
                hotel.city.contains(query, ignoreCase = true)
            }
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                    Log.w("FirestoreHotelRepository", "Index not found for search query. Please create composite index in Firebase Console: ${e.message}")
                    Log.w("FirestoreHotelRepository", "Index required: collection=hotels, fields=country(asc),city(asc),rating(desc)")
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
            val document = firestore.collection("hotels")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                val hotel = document.toObject(Hotel::class.java)?.copy(id = document.id)
                
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
                
                
                // Create a simple HotelDetail with basic info
                val hotelDetail = HotelDetail(
                    description = "A beautiful hotel in ${hotel?.city}, ${hotel?.country}",
                    facilities = listOf("WiFi", "Parking", "Restaurant", "Pool"),
                    photoUrls = (1..(hotel?.photoCount ?: 5)).map { "https://placehold.co/600x400" }
                )
                
                hotel?.copy(rooms = rooms, detail = hotelDetail)
            } else {
                null
            }
        } catch (e: Exception) {
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
                document.toObject(Hotel::class.java)?.copy(id = document.id)
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
}
