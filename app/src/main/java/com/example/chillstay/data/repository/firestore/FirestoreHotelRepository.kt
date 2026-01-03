package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Coordinate
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Policy
import com.example.chillstay.domain.model.PropertyType
import com.example.chillstay.domain.repository.HotelRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreHotelRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HotelRepository {

    // Cache để tránh query trùng lặp
    private val roomCache = mutableMapOf<String, Map<String, Any?>>()

    companion object {
        private const val TAG = "FirestoreHotelRepo"
        private const val WHERE_IN_MAX = 10
    }

    override suspend fun getHotels(): List<Hotel> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to load hotels...")
            val startTime = System.currentTimeMillis()

            // BƯỚC 1: Load tất cả hotels
            val snapshot = firestore.collection("hotels")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()

            val hotelDocs = snapshot.documents
            Log.d(TAG, "Loaded ${hotelDocs.size} hotel documents")

            if (hotelDocs.isEmpty()) {
                return@withContext emptyList()
            }

            // BƯỚC 2: Collect unique room IDs (nếu hotels có roomIds field)
            val allRoomIds = hotelDocs.flatMap { doc ->
                (doc.get("roomIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            }.distinct()

            Log.d(TAG, "Unique room IDs: ${allRoomIds.size}")

            // BƯỚC 3: Load room data SONG SONG (nếu cần)
            if (allRoomIds.isNotEmpty()) {
                loadRoomData(allRoomIds)
            }

            // BƯỚC 4: Map sang Hotel
            val hotels = withContext(Dispatchers.Default) {
                hotelDocs.mapNotNull { doc ->
                    mapHotelDocument(doc)?.copy(id = doc.id)
                }
            }

            val endTime = System.currentTimeMillis()
            Log.d(TAG, "Completed in ${endTime - startTime}ms - Total: ${hotels.size} hotels")

            hotels
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hotels: ${e.message}", e)
            emptyList()
        }
    }

    private suspend fun loadRoomData(roomIds: List<String>) {
        if (roomIds.isEmpty()) return

        val idsToFetch = roomIds.filterNot { roomCache.containsKey(it) }
        if (idsToFetch.isEmpty()) return

        val chunks = idsToFetch.chunked(WHERE_IN_MAX)

        supervisorScope {
            chunks.map { chunk ->
                async {
                    try {
                        val querySnapshot = firestore.collection("rooms")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()

                        for (doc in querySnapshot.documents) {
                            roomCache[doc.id] = doc.data ?: emptyMap()
                        }

                        // Mark missing docs
                        val returnedIds = querySnapshot.documents.map { it.id }.toSet()
                        chunk.forEach { id ->
                            if (!returnedIds.contains(id) && !roomCache.containsKey(id)) {
                                roomCache[id] = emptyMap()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading rooms for chunk: ${e.message}")
                        chunk.forEach { id ->
                            if (!roomCache.containsKey(id)) {
                                roomCache[id] = emptyMap()
                            }
                        }
                    }
                }
            }.forEach { it.await() }
        }
    }

    override suspend fun getHotelById(id: String): Hotel? {
        return try {
            Log.d(TAG, "Fetching hotel from Firestore, hotelId=$id")
            val document = firestore.collection("hotels")
                .document(id)
                .get()
                .await()

            if (!document.exists()) {
                Log.w(TAG, "Hotel document not found for id=$id")
                return null
            }

            val hotel = mapHotelDocument(document)?.copy(id = document.id)
            if (hotel == null) {
                Log.w(TAG, "Failed to map hotel document id=$id to model")
                return null
            }
            hotel
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hotelId=$id from Firestore: ${e.message}", e)
            null
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

    private fun hotelToMap(hotel: Hotel): Map<String, Any?> {
        val coordMap = hotel.coordinate.let { mapOf("lat" to it.latitude, "lng" to it.longitude) }
        val policies = hotel.policy.map { mapOf("title" to it.title, "content" to it.content) }
        return mapOf(
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
        } catch (_: Exception) {
            emptyList()
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
            Log.w(TAG, "Document ${document.id} has no data")
            return null
        }

        val name = data["name"] as? String
        if (name.isNullOrBlank()) {
            Log.w(TAG, "Document ${document.id} missing name")
            return null
        }

        val coordinate = when (val coord = data["coordinate"]) {
            is GeoPoint -> Coordinate(coord.latitude, coord.longitude)
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
            Log.e(TAG, "Failed to update hotel aggregation for $hotelId: ${e.message}", e)
            false
        }
    }
}