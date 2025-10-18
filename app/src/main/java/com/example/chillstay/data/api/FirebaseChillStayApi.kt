package com.example.chillstay.data.api

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelDetail
import com.example.chillstay.domain.model.HotelInformation
import com.example.chillstay.domain.model.Address
import com.example.chillstay.domain.model.Location
import com.example.chillstay.domain.model.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseChillStayApi(
    private val firestore: FirebaseFirestore
) : ChillStayApi {
    override suspend fun getPopularHotels(limit: Int): List<Hotel> {
        val snapshot = firestore.collection("hotels")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.mapNotNull { mapHotel(it.id, it.data) }
    }

    override suspend fun getRecommendedHotels(limit: Int): List<Hotel> {
        val snapshot = firestore.collection("hotels")
            .orderBy("numberOfReviews", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.mapNotNull { mapHotel(it.id, it.data) }
    }

    override suspend fun getTrendingHotels(limit: Int): List<Hotel> {
        val snapshot = firestore.collection("hotels")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.mapNotNull { mapHotel(it.id, it.data) }
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
    val name = data["name"] as? String ?: return null
    val country = data["country"] as? String ?: ""
    val city = data["city"] as? String ?: ""
    val rating = (data["rating"] as? Number)?.toDouble() ?: 0.0
    val numberOfReviews = (data["numberOfReviews"] as? Number)?.toInt() ?: 0
    val imageUrl = data["imageUrl"] as? String ?: ""

    // Build a minimal placeholder detail to satisfy domain model
    val address = Address(country = country, city = city)
    val info = HotelInformation(numberOfBedrooms = 0, numberOfBathrooms = 0, squareMeters = 0)
    val location = Location(latitude = "0.0", longitude = "0.0")
    val detail = HotelDetail(
        address = address,
        description = "",
        photoUrls = emptyList(),
        hotelInformation = info,
        facilities = emptyList(),
        location = location,
        reviews = emptyList()
    )

    // Return top-level Hotel; avoid recursion by replacing nested hotel with a lightweight placeholder in detail
    return Hotel(
        id = id,
        name = name,
        country = country,
        city = city,
        rating = rating,
        numberOfReviews = numberOfReviews,
        imageUrl = imageUrl,
        detail = null,
        rooms = emptyList()
    )
}


