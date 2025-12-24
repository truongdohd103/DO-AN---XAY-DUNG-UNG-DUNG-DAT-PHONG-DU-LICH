package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.RoomRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRoomRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : RoomRepository {
    
    companion object {
        const val LOG_TAG = "FirestoreRoomRepository"
    }
    
    override suspend fun getRoomsByHotelId(
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun createRoom(room: Room): String {
        try {
            Log.d(LOG_TAG, "Creating room in Firestore")
            val data = roomToMap(room)
            val docRef = firestore.collection("rooms")
                .add(data)
                .await()
            Log.d(LOG_TAG, "Created room with id=${docRef.id}")
            return docRef.id
        } catch (e: FirebaseFirestoreException) {
            Log.e(LOG_TAG, "Firestore error creating room: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error creating room: ${e.message}", e)
            throw e
        }
    }

    private fun roomToMap(room: Room): Map<String, Any?> {
        val gallery = mapOf(
            "exteriorView" to room.gallery?.exteriorView,
            "dining" to room.gallery?.dining,
            "thisRoom" to room.gallery?.thisRoom
        )
        return mapOf(
            "hotelId" to room.hotelId,
            "name" to room.name,
            "area" to room.area,
            "doubleBed" to room.doubleBed,
            "singleBed" to room.singleBed,
            "quantity" to room.quantity,
            "features" to room.feature,
            "breakfastPrice" to room.breakfastPrice,
            "price" to room.price,
            "discount" to room.discount,
            "quantity" to room.quantity,
            "capacity" to room.capacity,
            "gallery" to gallery,
            "status" to room.status.name
        )
    }

    override suspend fun updateRoom(room: Room) {
        try {
            Log.d(LOG_TAG, "Updating hotel id=${room.id}")
            val data = roomToMap(room)
            firestore.collection("rooms")
                .document(room.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d(LOG_TAG, "Updated room id=${room.id}")
        } catch (e: FirebaseFirestoreException) {
            Log.e(LOG_TAG, "Firestore error updating room id=${room.id}: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error updating room id=${room.id}: ${e.message}", e)
            throw e
        }
    }
}