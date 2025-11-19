package com.example.chillstay.data.api

import android.util.Log
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Coordinate
import com.example.chillstay.domain.model.Policy
import com.example.chillstay.domain.model.PropertyType
import com.example.chillstay.domain.model.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseChillStayApi(
    private val firestore: FirebaseFirestore
) : ChillStayApi {
    override suspend fun getPopularHotels(limit: Int): List<Hotel> {
        Log.d("FirebaseChillStayApi", "Fetching popular hotels (limit: $limit)")
        return try {
            val snapshot = firestore.collection("hotels")
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            Log.d("FirebaseChillStayApi", "Successfully fetched ${snapshot.documents.size} documents from Firestore for popular hotels")
            val hotels = snapshot.documents.mapNotNull { doc ->
                Log.d("FirebaseChillStayApi", "Processing document ID: ${doc.id}, has data: ${doc.data != null}")
                val hotel = mapHotel(doc.id, doc.data)
                if (hotel == null) {
                    Log.w("FirebaseChillStayApi", "Failed to map hotel from document ID: ${doc.id}")
                } else {
                    Log.d("FirebaseChillStayApi", "Successfully mapped hotel: ${hotel.name} (ID: ${hotel.id})")
                }
                hotel
            }
            Log.d("FirebaseChillStayApi", "Returning ${hotels.size} popular hotels")
            hotels
        } catch (e: Exception) {
            Log.e("FirebaseChillStayApi", "Error fetching popular hotels: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getRecommendedHotels(limit: Int): List<Hotel> {
        Log.d("FirebaseChillStayApi", "Fetching recommended hotels (limit: $limit)")
        return try {
            val snapshot = firestore.collection("hotels")
                .orderBy("numberOfReviews", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            Log.d("FirebaseChillStayApi", "Successfully fetched ${snapshot.documents.size} documents from Firestore for recommended hotels")
            val hotels = snapshot.documents.mapNotNull { doc ->
                Log.d("FirebaseChillStayApi", "Processing document ID: ${doc.id}, has data: ${doc.data != null}")
                val hotel = mapHotel(doc.id, doc.data)
                if (hotel == null) {
                    Log.w("FirebaseChillStayApi", "Failed to map hotel from document ID: ${doc.id}")
                } else {
                    Log.d("FirebaseChillStayApi", "Successfully mapped hotel: ${hotel.name} (ID: ${hotel.id})")
                }
                hotel
            }
            Log.d("FirebaseChillStayApi", "Returning ${hotels.size} recommended hotels")
            hotels
        } catch (e: Exception) {
            Log.e("FirebaseChillStayApi", "Error fetching recommended hotels: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getTrendingHotels(limit: Int): List<Hotel> {
        Log.d("FirebaseChillStayApi", "Fetching trending hotels (limit: $limit)")
        return try {
            val snapshot = firestore.collection("hotels")
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            Log.d("FirebaseChillStayApi", "Successfully fetched ${snapshot.documents.size} documents from Firestore for trending hotels")
            val hotels = snapshot.documents.mapNotNull { doc ->
                Log.d("FirebaseChillStayApi", "Processing document ID: ${doc.id}, has data: ${doc.data != null}")
                val hotel = mapHotel(doc.id, doc.data)
                if (hotel == null) {
                    Log.w("FirebaseChillStayApi", "Failed to map hotel from document ID: ${doc.id}")
                } else {
                    Log.d("FirebaseChillStayApi", "Successfully mapped hotel: ${hotel.name} (ID: ${hotel.id})")
                }
                hotel
            }
            Log.d("FirebaseChillStayApi", "Returning ${hotels.size} trending hotels")
            hotels
        } catch (e: Exception) {
            Log.e("FirebaseChillStayApi", "Error fetching trending hotels: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getHotelById(hotelId: String): Hotel? {
        val doc = firestore.collection("hotels").document(hotelId).get().await()
        return if (doc.exists()) mapHotel(doc.id, doc.data) else null
    }

    override suspend fun getRooms(hotelId: String): List<Room> {
        val snapshot = firestore.collection("rooms")
            .whereEqualTo("hotelId", hotelId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Room::class.java)?.copy(id = it.id) }
    }

    override suspend fun getUserBookings(userId: String): List<Booking> {
        val snapshot = firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Booking::class.java)?.copy(id = it.id) }
    }

    override suspend fun getUserBookmarks(userId: String): List<Bookmark> {
        val snapshot = firestore.collection("bookmarks")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Bookmark::class.java)?.copy(id = it.id) }
    }
}

private fun mapHotel(id: String, data: Map<String, Any>?): Hotel? {
    if (data == null) return null

    // Check required fields
    val name = data["name"] as? String
    if (name.isNullOrBlank()) return null
    else Log.d("HotelName", name)

    // Log field presence for debugging
    Log.d("FirebaseChillStayApi", "mapHotel: Mapping hotel '$name' (ID: $id)")
    Log.d("FirebaseChillStayApi", "  - city: ${data["city"] as? String ?: "MISSING"}")
    Log.d("FirebaseChillStayApi", "  - country: ${data["country"] as? String ?: "MISSING"}")
    Log.d("FirebaseChillStayApi", "  - description: ${if (data["description"] != null) "PRESENT (${(data["description"] as? String)?.length ?: 0} chars)" else "MISSING"}")
    Log.d("FirebaseChillStayApi", "  - imageUrl: ${if (data["imageUrl"] != null) "PRESENT (${(data["imageUrl"] as? List<*>)?.size ?: 0} items)" else "MISSING"}")
    Log.d("FirebaseChillStayApi", "  - rating: ${data["rating"]}")
    Log.d("FirebaseChillStayApi", "  - numberOfReviews: ${data["numberOfReviews"]}")

    // Handle coordinate - could be GeoPoint or Map
    val coordinate = when (val coord = data["coordinate"]) {
        is com.google.firebase.firestore.GeoPoint -> {
            Coordinate(latitude = coord.latitude, longitude = coord.longitude)
        }
        is Map<*, *> -> {
            val lat = (coord["latitude"] as? Number)?.toDouble() ?: (coord["lat"] as? Number)?.toDouble() ?: 0.0
            val lng = (coord["longitude"] as? Number)?.toDouble() ?: (coord["lng"] as? Number)?.toDouble() ?: 0.0
            Coordinate(latitude = lat, longitude = lng)
        }
        else -> Coordinate(latitude = 0.0, longitude = 0.0)
    }

    return Hotel(
        id = id,
        coordinate = coordinate,
        city = data["city"] as? String ?: "",
        country = data["country"] as? String ?: "",
        description = data["description"] as? String ?: "",
        feature = (data["feature"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        formattedAddress = data["formattedAddress"] as? String ?: "",
        imageUrl = (data["imageUrl"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        language = (data["language"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        minPrice = (data["minPrice"] as? Number)?.toDouble(),
        name = name,
        numberOfReviews = (data["numberOfReviews"] as? Number)?.toInt() ?: 0,
        policy = (data["policy"] as? List<*>)?.mapNotNull {
            val policyMap = it as? Map<*, *>
            val title = policyMap?.get("title") as? String ?: ""
            val content = policyMap?.get("content") as? String ?: ""
            Policy(title = title, content = content)
        } ?: emptyList(),
        propertyType = when (data["propertyType"] as? String) {
            "RESORT" -> PropertyType.RESORT
            else -> PropertyType.HOTEL
        },
        rating = (data["rating"] as? Number)?.toDouble() ?: 0.0
    )
}


