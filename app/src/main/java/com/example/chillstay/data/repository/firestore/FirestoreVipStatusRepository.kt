package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.domain.model.VipStatusHistory
import com.example.chillstay.domain.model.VipBenefit
import com.example.chillstay.domain.model.VipLevel
import com.example.chillstay.domain.model.VipAction
import com.example.chillstay.domain.repository.VipStatusRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreVipStatusRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : VipStatusRepository {

    override suspend fun getVipStatus(userId: String): VipStatus? {
        return try {
            Log.d("FirestoreVipStatusRepository", "Fetching VIP status for user: $userId")
            val document = firestore.collection("vip_status")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val vipStatus = document.toObject(VipStatus::class.java)?.copy(id = document.id)
                Log.d("FirestoreVipStatusRepository", "Successfully fetched VIP status: ${vipStatus?.level}")
                vipStatus
            } else {
                Log.d("FirestoreVipStatusRepository", "VIP status not found for user: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error fetching VIP status: ${e.message}", e)
            null
        }
    }

    override suspend fun createVipStatus(vipStatus: VipStatus): VipStatus {
        return try {
            Log.d("FirestoreVipStatusRepository", "Creating VIP status for user: ${vipStatus.userId}")
            firestore.collection("vip_status")
                .document(vipStatus.userId)
                .set(vipStatus)
                .await()
            Log.d("FirestoreVipStatusRepository", "Successfully created VIP status")
            vipStatus
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error creating VIP status: ${e.message}", e)
            vipStatus
        }
    }

    override suspend fun updateVipStatus(vipStatus: VipStatus): VipStatus {
        return try {
            Log.d("FirestoreVipStatusRepository", "Updating VIP status for user: ${vipStatus.userId}")
            firestore.collection("vip_status")
                .document(vipStatus.userId)
                .set(vipStatus)
                .await()
            Log.d("FirestoreVipStatusRepository", "Successfully updated VIP status")
            vipStatus
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error updating VIP status: ${e.message}", e)
            vipStatus
        }
    }

    override suspend fun addPoints(userId: String, points: Int, reason: String, bookingId: String?): Boolean {
        return try {
            Log.d("FirestoreVipStatusRepository", "Adding $points points for user: $userId")
            
            val currentStatus = getVipStatus(userId) ?: return false
            val newPoints = currentStatus.points + points
            val newLevel = calculateVipLevel(newPoints)
            
            val updatedStatus = currentStatus.copy(
                points = newPoints,
                level = newLevel,
                lastActivity = com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )
            
            updateVipStatus(updatedStatus)
            
            // Add to history
            val history = VipStatusHistory(
                userId = userId,
                action = VipAction.POINTS_EARNED,
                pointsChange = points,
                description = reason,
                bookingId = bookingId
            )
            firestore.collection("vip_status_history").add(history).await()
            
            Log.d("FirestoreVipStatusRepository", "Successfully added points")
            true
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error adding points: ${e.message}", e)
            false
        }
    }

    override suspend fun redeemPoints(userId: String, points: Int, reason: String): Boolean {
        return try {
            Log.d("FirestoreVipStatusRepository", "Redeeming $points points for user: $userId")
            
            val currentStatus = getVipStatus(userId) ?: return false
            if (currentStatus.points < points) {
                Log.w("FirestoreVipStatusRepository", "Insufficient points for redemption")
                return false
            }
            
            val newPoints = currentStatus.points - points
            val newLevel = calculateVipLevel(newPoints)
            
            val updatedStatus = currentStatus.copy(
                points = newPoints,
                level = newLevel,
                lastActivity = com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )
            
            updateVipStatus(updatedStatus)
            
            // Add to history
            val history = VipStatusHistory(
                userId = userId,
                action = VipAction.POINTS_REDEEMED,
                pointsChange = -points,
                description = reason
            )
            firestore.collection("vip_status_history").add(history).await()
            
            Log.d("FirestoreVipStatusRepository", "Successfully redeemed points")
            true
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error redeeming points: ${e.message}", e)
            false
        }
    }

    override suspend fun getVipStatusHistory(userId: String): List<VipStatusHistory> {
        return try {
            Log.d("FirestoreVipStatusRepository", "Fetching VIP status history for user: $userId")
            val snapshot = firestore.collection("vip_status_history")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val history = snapshot.documents.mapNotNull { document ->
                document.toObject(VipStatusHistory::class.java)?.copy(id = document.id)
            }
            Log.d("FirestoreVipStatusRepository", "Successfully fetched ${history.size} history records")
            history
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error fetching VIP status history: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getVipBenefits(level: VipLevel): List<VipBenefit> {
        return try {
            Log.d("FirestoreVipStatusRepository", "Fetching VIP benefits for level: $level")
            val snapshot = firestore.collection("vip_benefits")
                .whereEqualTo("level", level.name)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val benefits = snapshot.documents.mapNotNull { document ->
                document.toObject(VipBenefit::class.java)?.copy(id = document.id)
            }
            Log.d("FirestoreVipStatusRepository", "Successfully fetched ${benefits.size} benefits")
            benefits
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error fetching VIP benefits: ${e.message}", e)
            getDefaultBenefits(level)
        }
    }

    override suspend fun calculateVipLevel(points: Int): VipLevel {
        return when {
            points >= VipLevel.DIAMOND.minPoints -> VipLevel.DIAMOND
            points >= VipLevel.PLATINUM.minPoints -> VipLevel.PLATINUM
            points >= VipLevel.GOLD.minPoints -> VipLevel.GOLD
            points >= VipLevel.SILVER.minPoints -> VipLevel.SILVER
            else -> VipLevel.BRONZE
        }
    }

    override suspend fun getNextLevelPoints(currentLevel: VipLevel): Int {
        return when (currentLevel) {
            VipLevel.BRONZE -> VipLevel.SILVER.minPoints
            VipLevel.SILVER -> VipLevel.GOLD.minPoints
            VipLevel.GOLD -> VipLevel.PLATINUM.minPoints
            VipLevel.PLATINUM -> VipLevel.DIAMOND.minPoints
            VipLevel.DIAMOND -> VipLevel.DIAMOND.minPoints // Max level
        }
    }

    override suspend fun addVipStatusHistory(history: VipStatusHistory) {
        try {
            firestore.collection("vip_status_history").add(history).await()
        } catch (e: Exception) {
            Log.e("FirestoreVipStatusRepository", "Error adding VIP history: ${e.message}", e)
        }
    }

    private fun getDefaultBenefits(level: VipLevel): List<VipBenefit> {
        return when (level) {
            VipLevel.BRONZE -> listOf(
                VipBenefit(
                    title = "5% Discount",
                    description = "Get 5% off on all bookings",
                    icon = "🎯",
                    level = level
                ),
                VipBenefit(
                    title = "Priority Support",
                    description = "Get priority customer support",
                    icon = "🎧",
                    level = level
                )
            )
            VipLevel.SILVER -> listOf(
                VipBenefit(
                    title = "10% Discount",
                    description = "Get 10% off on all bookings",
                    icon = "🎯",
                    level = level
                ),
                VipBenefit(
                    title = "Free Cancellation",
                    description = "Free cancellation up to 24 hours",
                    icon = "🔄",
                    level = level
                ),
                VipBenefit(
                    title = "Priority Support",
                    description = "Get priority customer support",
                    icon = "🎧",
                    level = level
                )
            )
            VipLevel.GOLD -> listOf(
                VipBenefit(
                    title = "15% Discount",
                    description = "Get 15% off on all bookings",
                    icon = "🎯",
                    level = level
                ),
                VipBenefit(
                    title = "Free Upgrades",
                    description = "Free room upgrades when available",
                    icon = "⬆️",
                    level = level
                ),
                VipBenefit(
                    title = "Late Checkout",
                    description = "Late checkout until 2 PM",
                    icon = "🕐",
                    level = level
                ),
                VipBenefit(
                    title = "Priority Support",
                    description = "Get priority customer support",
                    icon = "🎧",
                    level = level
                )
            )
            VipLevel.PLATINUM -> listOf(
                VipBenefit(
                    title = "20% Discount",
                    description = "Get 20% off on all bookings",
                    icon = "🎯",
                    level = level
                ),
                VipBenefit(
                    title = "Free Upgrades",
                    description = "Free room upgrades when available",
                    icon = "⬆️",
                    level = level
                ),
                VipBenefit(
                    title = "Late Checkout",
                    description = "Late checkout until 4 PM",
                    icon = "🕐",
                    level = level
                ),
                VipBenefit(
                    title = "Welcome Amenities",
                    description = "Complimentary welcome amenities",
                    icon = "🎁",
                    level = level
                ),
                VipBenefit(
                    title = "Priority Support",
                    description = "Get priority customer support",
                    icon = "🎧",
                    level = level
                )
            )
            VipLevel.DIAMOND -> listOf(
                VipBenefit(
                    title = "25% Discount",
                    description = "Get 25% off on all bookings",
                    icon = "🎯",
                    level = level
                ),
                VipBenefit(
                    title = "Free Upgrades",
                    description = "Free room upgrades when available",
                    icon = "⬆️",
                    level = level
                ),
                VipBenefit(
                    title = "Late Checkout",
                    description = "Late checkout until 6 PM",
                    icon = "🕐",
                    level = level
                ),
                VipBenefit(
                    title = "Welcome Amenities",
                    description = "Complimentary welcome amenities",
                    icon = "🎁",
                    level = level
                ),
                VipBenefit(
                    title = "Personal Concierge",
                    description = "Dedicated personal concierge service",
                    icon = "👤",
                    level = level
                ),
                VipBenefit(
                    title = "Priority Support",
                    description = "Get priority customer support",
                    icon = "🎧",
                    level = level
                )
            )
        }
    }
}

