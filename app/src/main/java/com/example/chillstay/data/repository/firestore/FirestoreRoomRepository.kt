package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomGallery
import com.example.chillstay.domain.model.RoomStatus
import com.example.chillstay.domain.repository.RoomRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.min

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
                val data = document.data ?: return@mapNotNull null

                // Một số field có thể nằm trong map "detail"
                val detail = data["detail"] as? Map<*, *>

                // Map các field từ Firestore (tên field có thể khác với model)
                val name = (data["name"] as? String)
                    ?: (detail?.get("name") as? String)
                    ?: ""

                val area = (data["size"] as? Number)?.toDouble()
                    ?: (detail?.get("size") as? Number)?.toDouble()
                    ?: (data["area"] as? Number)?.toDouble()
                    ?: 0.0

                val capacity = (data["capacity"] as? Number)?.toInt()
                    ?: (detail?.get("capacity") as? Number)?.toInt()
                    ?: 0

                val price = (data["price"] as? Number)?.toDouble()
                    ?: (detail?.get("price") as? Number)?.toDouble()
                    ?: 0.0

                val availableCount = (data["availableCount"] as? Number)?.toInt()
                    ?: (data["quantity"] as? Number)?.toInt()
                    ?: 0

                // Map facilities (Firestore dùng "facilities", model dùng "feature")
                val facilities = (data["facilities"] as? List<*>)?.mapNotNull { it as? String }
                    ?: (detail?.get("facilities") as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList()

                // Map gallery
                val galleryMap = data["gallery"] as? Map<*, *>
                val gallery = galleryMap?.let {
                    RoomGallery(
                        exteriorView = (it["exteriorView"] as? List<*>)?.mapNotNull { url -> url as? String }
                            ?: emptyList(),
                        dining = (it["dining"] as? List<*>)?.mapNotNull { url -> url as? String }
                            ?: emptyList(),
                        thisRoom = (it["thisRoom"] as? List<*>)?.mapNotNull { url -> url as? String }
                            ?: emptyList()
                    )
                }

                // Map các field khác
                val doubleBed = (data["doubleBed"] as? Number)?.toInt()
                    ?: (detail?.get("doubleBed") as? Number)?.toInt()
                    ?: 0

                val singleBed = (data["singleBed"] as? Number)?.toInt()
                    ?: (detail?.get("singleBed") as? Number)?.toInt()
                    ?: 0

                val quantity = (data["quantity"] as? Number)?.toInt() ?: 0
                val breakfastPrice = (data["breakfastPrice"] as? Number)?.toDouble()
                    ?: (detail?.get("breakfastPrice") as? Number)?.toDouble()
                    ?: 0.0

                val discount = (data["discount"] as? Number)?.toDouble()
                    ?: (detail?.get("discount") as? Number)?.toDouble()
                    ?: 0.0

                val status = when ((data["status"] as? String)?.uppercase()) {
                    "INACTIVE" -> RoomStatus.INACTIVE
                    else -> RoomStatus.ACTIVE
                }

                Room(
                    id = document.id,
                    hotelId = data["hotelId"] as? String ?: "",
                    name = name,
                    area = area,
                    doubleBed = doubleBed,
                    singleBed = singleBed,
                    quantity = quantity,
                    availableCount = availableCount,
                    feature = facilities,
                    breakfastPrice = breakfastPrice,
                    price = price,
                    discount = discount,
                    capacity = capacity,
                    gallery = gallery,
                    status = status
                )
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
                val data = document.data ?: return null

                val detail = data["detail"] as? Map<*, *>

                val name = (data["name"] as? String)
                    ?: (detail?.get("name") as? String)
                    ?: ""

                val area = (data["size"] as? Number)?.toDouble()
                    ?: (detail?.get("size") as? Number)?.toDouble()
                    ?: (data["area"] as? Number)?.toDouble()
                    ?: 0.0

                val capacity = (data["capacity"] as? Number)?.toInt()
                    ?: (detail?.get("capacity") as? Number)?.toInt()
                    ?: 0

                val price = (data["price"] as? Number)?.toDouble()
                    ?: (detail?.get("price") as? Number)?.toDouble()
                    ?: 0.0

                val availableCount = (data["availableCount"] as? Number)?.toInt()
                    ?: (data["quantity"] as? Number)?.toInt()
                    ?: 0

                val facilities = (data["facilities"] as? List<*>)?.mapNotNull { it as? String }
                    ?: (detail?.get("facilities") as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList()

                val galleryMap = data["gallery"] as? Map<*, *>
                val gallery = galleryMap?.let {
                    RoomGallery(
                        exteriorView = (it["exteriorView"] as? List<*>)?.mapNotNull { url -> url as? String }
                            ?: emptyList(),
                        dining = (it["dining"] as? List<*>)?.mapNotNull { url -> url as? String }
                            ?: emptyList(),
                        thisRoom = (it["thisRoom"] as? List<*>)?.mapNotNull { url -> url as? String }
                            ?: emptyList()
                    )
                }

                val doubleBed = (data["doubleBed"] as? Number)?.toInt()
                    ?: (detail?.get("doubleBed") as? Number)?.toInt()
                    ?: 0

                val singleBed = (data["singleBed"] as? Number)?.toInt()
                    ?: (detail?.get("singleBed") as? Number)?.toInt()
                    ?: 0

                val quantity = (data["quantity"] as? Number)?.toInt() ?: 0
                val breakfastPrice = (data["breakfastPrice"] as? Number)?.toDouble()
                    ?: (detail?.get("breakfastPrice") as? Number)?.toDouble()
                    ?: 0.0

                val discount = (data["discount"] as? Number)?.toDouble()
                    ?: (detail?.get("discount") as? Number)?.toDouble()
                    ?: 0.0

                val status = when ((data["status"] as? String)?.uppercase()) {
                    "INACTIVE" -> RoomStatus.INACTIVE
                    else -> RoomStatus.ACTIVE
                }

                Room(
                    id = document.id,
                    hotelId = data["hotelId"] as? String ?: "",
                    name = name,
                    area = area,
                    doubleBed = doubleBed,
                    singleBed = singleBed,
                    quantity = quantity,
                    availableCount = availableCount,
                    feature = facilities,
                    breakfastPrice = breakfastPrice,
                    price = price,
                    discount = discount,
                    capacity = capacity,
                    gallery = gallery,
                    status = status
                )
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

    // Helper: recompute minPrice by scanning rooms of a hotel
    private suspend fun recomputeMinPriceForHotel(hotelId: String) {
        try {
            val roomsSnap = firestore.collection("rooms")
                .whereEqualTo("hotelId", hotelId)
                .get()
                .await()

            val minPrice: Double? = roomsSnap.documents
                .mapNotNull { it.getDouble("price") }
                .minOrNull()

            val hotelRef = firestore.collection("hotels").document(hotelId)
            if (minPrice == null) {
                // No rooms -> remove minPrice or set null
                hotelRef.update("minPrice", null).await()
                Log.d(LOG_TAG, "Recomputed minPrice for hotelId=$hotelId -> null (no rooms)")
            } else {
                hotelRef.update("minPrice", minPrice).await()
                Log.d(LOG_TAG, "Recomputed minPrice for hotelId=$hotelId -> $minPrice")
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to recompute minPrice for hotelId=$hotelId: ${e.message}", e)
        }
    }

    override suspend fun createRoom(room: Room): String {
        try {
            Log.d(LOG_TAG, "Creating room in Firestore (with hotel update)")

            // prepare new docRef with client-generated id so we can write it inside transaction
            val docRef = firestore.collection("rooms").document()
            val newRoomId = docRef.id

            // Build data map (roomToMap should not include id, or if it does, override)
            val data = roomToMap(room).toMutableMap()
            // ensure id and hotelId are present
            data["hotelId"] = room.hotelId
            // optionally set id field in room doc if you store it there
            data["id"] = newRoomId
            // Khi tạo room mới, availableCount = quantity (tất cả phòng đều available)
            data["availableCount"] = room.quantity
            data["isAvailable"] = room.quantity > 0

            // run transaction: create room doc and update hotel's roomIds + minPrice
            firestore.runTransaction { transaction ->
                // create room doc
                transaction.set(docRef, data)

                // update hotel doc: add room id to roomIds array and update minPrice if needed
                val hotelRef = firestore.collection("hotels").document(room.hotelId)
                val hotelSnap = try {
                    transaction.get(hotelRef)
                } catch (ex: Exception) {
                    // hotel not found or read error; let it propagate out of transaction
                    throw ex
                }

                if (!hotelSnap.exists()) {
                    // If hotel doesn't exist — we could throw or just create minimal hotel fields.
                    // Here we choose to create a minimal hotel doc with roomIds and minPrice
                    transaction.set(hotelRef, mapOf(
                        "roomIds" to listOf(newRoomId),
                        "minPrice" to room.price
                    ), SetOptions.merge())
                } else {
                    val currentMin = hotelSnap.getDouble("minPrice")
                    val newPrice = room.price
                    val computedMin = if (currentMin == null) {
                        newPrice
                    } else run {
                        min(currentMin, newPrice)
                    }

                    // Add the room id into roomIds atomically
                    transaction.update(hotelRef, "roomIds", FieldValue.arrayUnion(newRoomId))

                    // If computedMin is not null, update minPrice
                    transaction.update(hotelRef, "minPrice", computedMin)
                }

                // return the new id as transaction result
                newRoomId
            }.await().also {
                Log.d(LOG_TAG, "Created room with id=$it and updated hotel=${room.hotelId}")
            }

            return newRoomId
        } catch (e: FirebaseFirestoreException) {
            Log.e(LOG_TAG, "Firestore error creating room: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error creating room: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateRoom(room: Room) {
        try {
            Log.d(LOG_TAG, "Updating room id=${room.id} (and update hotel if needed)")

            val roomRef = firestore.collection("rooms").document(room.id)
            val hotelRef = firestore.collection("hotels").document(room.hotelId)

            // Prepare map to write (similar to your previous roomToMap)
            val data = roomToMap(room).toMutableMap()

            // We'll track whether we need to recompute minPrice after transaction
            var needRecomputeMinAfterTxn = false
            var hotelIdForRecompute: String? = null

            // Run transaction to update room and update hotel's minPrice optimistically
            firestore.runTransaction { transaction ->
                // Read the existing room (may throw if not exists)
                val oldRoomSnap = transaction.get(roomRef)
                // Read hotel
                val hotelSnap = transaction.get(hotelRef)
                val oldPrice = oldRoomSnap.getDouble("price")
                val oldQuantity = oldRoomSnap.getLong("quantity")?.toInt() ?: 0
                val oldAvailableCount = oldRoomSnap.getLong("availableCount")?.toInt() ?: oldQuantity

                // Nếu quantity thay đổi, cần điều chỉnh availableCount
                val newQuantity = room.quantity
                val newAvailableCount = if (newQuantity != oldQuantity) {
                    // Nếu quantity tăng, tăng availableCount tương ứng
                    // Nếu quantity giảm, giảm availableCount nhưng không được < 0
                    val diff = newQuantity - oldQuantity
                    val adjusted = oldAvailableCount + diff
                    maxOf(0, minOf(adjusted, newQuantity))
                } else {
                    // Giữ nguyên availableCount nếu quantity không đổi
                    room.availableCount
                }
                
                // Cập nhật availableCount trong data
                data["availableCount"] = newAvailableCount
                data["isAvailable"] = newAvailableCount > 0

                // Update the room (merge semantics)
                // Transaction.set with SetOptions.merge is allowed
                transaction.set(roomRef, data, SetOptions.merge())

                val currentMin = hotelSnap.getDouble("minPrice")

                val newPrice = room.price

                // If no currentMin -> set it to newPrice if newPrice != null
                if (currentMin == null) {
                    transaction.update(hotelRef, "minPrice", newPrice)
                } else {
                    if (newPrice < currentMin) {
                        // new price becomes new min
                        transaction.update(hotelRef, "minPrice", newPrice)
                    } else {
                        // newPrice >= currentMin
                        // but if oldPrice equals currentMin and newPrice > oldPrice then min may need recompute
                        if (oldPrice != null && oldPrice == currentMin && newPrice > oldPrice) {
                            // mark to recompute min AFTER transaction (can't query inside transaction)
                            needRecomputeMinAfterTxn = true
                            hotelIdForRecompute = room.hotelId
                            // we don't change min here; we will recompute after txn completes
                        }
                        // otherwise nothing to do
                    }
                }
                // Return something (unused)
                null
            }.await()

            Log.d(LOG_TAG, "Updated room id=${room.id}. recomputeNeeded=$needRecomputeMinAfterTxn")

            // If needed, recompute minPrice by scanning rooms
            if (needRecomputeMinAfterTxn && hotelIdForRecompute != null) {
                recomputeMinPriceForHotel(hotelIdForRecompute)
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(LOG_TAG, "Firestore error updating room id=${room.id}: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error updating room id=${room.id}: ${e.message}", e)
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
            "availableCount" to room.availableCount,
            "features" to room.feature,
            "breakfastPrice" to room.breakfastPrice,
            "price" to room.price,
            "discount" to room.discount,
            "capacity" to room.capacity,
            "gallery" to gallery,
            "status" to room.status.name,
            "isAvailable" to (room.availableCount > 0)
        )
    }
}