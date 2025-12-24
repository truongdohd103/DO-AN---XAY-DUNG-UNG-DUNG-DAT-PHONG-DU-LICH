package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Coordinate
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Policy
import com.example.chillstay.domain.model.PropertyType
import com.example.chillstay.domain.model.Room
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

    override suspend fun getHotels(): List<Hotel> {
        return try {
            Log.d("FirestoreHotelRepository", "Attempting to fetch hotels from Firestore")
            val snapshot = firestore.collection("hotels")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d(
                "FirestoreHotelRepository",
                "Successfully fetched ${snapshot.documents.size} hotels"
            )
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
                    Log.w(
                        "FirestoreHotelRepository",
                        "Index not found for hotels query. Please create index in Firebase Console: ${e.message}"
                    )
                    Log.w(
                        "FirestoreHotelRepository",
                        "Index required: collection=hotels, fields=rating(desc)"
                    )
                    emptyList()
                }

                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w(
                        "FirestoreHotelRepository",
                        "Permission denied accessing hotels: ${e.message}"
                    )
                    emptyList()
                }

                else -> {
                    Log.e(
                        "FirestoreHotelRepository",
                        "Firestore error fetching hotels: ${e.message}",
                        e
                    )
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreHotelRepository", "Unexpected error fetching hotels: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun createHotel(hotel: Hotel): String {
        val data = hotelToMap(hotel)
        val docRef = firestore.collection("hotels")
            .add(data)
            .await()
        return docRef.id

    }

    override suspend fun updateHotel(hotel: Hotel) {
        val docRef = firestore.collection("hotels").document(hotel.id)
        val data = hotelToMap(hotel)
        docRef.set(data, SetOptions.merge()).await()
    }

    // Thêm log vào hàm hotelToMap
    private fun hotelToMap(hotel: Hotel): Map<String, Any?> {
        val coordMap = hotel.coordinate.let { mapOf("lat" to it.latitude, "lng" to it.longitude) }
        val policies = hotel.policy.map { mapOf("title" to it.title, "content" to it.content) }
        val result = mapOf(
            "name" to hotel.name,
            "description" to hotel.description,
            "propertyType" to hotel.propertyType.name,
            "formattedAddress" to hotel.formattedAddress,
            "country" to hotel.country,
            "city" to hotel.city,
            "coordinate" to coordMap,
            "imageUrl" to hotel.imageUrl,
            "policy" to policies,
            "language" to hotel.language,
            "feature" to hotel.feature,
            "minPrice" to hotel.minPrice,
            "rating" to hotel.rating,
            "numberOfReviews" to hotel.numberOfReviews
        )
        return result
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
            val mapped = snapshot.documents.mapNotNull { document ->
                mapHotelDocument(document)
            }
            val q = query.trim()
            val filtered = mapped
                .filter { hotel ->
                    q.isEmpty() ||
                            hotel.name.contains(q, ignoreCase = true) ||
                            hotel.city.contains(q, ignoreCase = true) ||
                            hotel.country.contains(q, ignoreCase = true) ||
                            hotel.description.contains(q, ignoreCase = true)
                }
            filtered
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> { emptyList() }
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> { emptyList() }
                else -> { emptyList() }
            }
        } catch (_: Exception) { emptyList() }
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
        } catch (_: Exception) {
            emptyList()
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
            feature = (data["feature"] as? List<*>)?.mapNotNull { it as? String }
                ?: emptyList(),
            formattedAddress = data["formattedAddress"] as? String ?: "",
            imageUrl = (data["imageUrl"] as? List<*>)?.mapNotNull { it as? String }
                ?: emptyList(),
            language = (data["language"] as? List<*>)?.mapNotNull { it as? String }
                ?: emptyList(),
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


    override suspend fun updateHotelAggregation(
        hotelId: String,
        rating: Double,
        numberOfReviews: Int
    ): Boolean {
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
            Log.e(
                "FirestoreHotelRepository",
                "Failed to update hotel aggregation for $hotelId: ${e.message}",
                e
            )
            false
        }
    }
}
