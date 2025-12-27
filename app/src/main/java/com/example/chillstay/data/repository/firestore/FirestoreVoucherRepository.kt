package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreVoucherRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : VoucherRepository {

    companion object {
        const val ACCOM_TAG = "AccommodationSelect"
    }

    override suspend fun getVouchers(): List<Voucher> {
        return try {
            Log.d("FirestoreVoucherRepository", "Fetching vouchers from Firestore")
            val snapshot = firestore.collection("vouchers")
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            val vouchers = snapshot.documents.mapNotNull { document ->
                document.toObject(Voucher::class.java)?.copy(id = document.id)
            }
            Log.d("FirestoreVoucherRepository", "Successfully fetched ${vouchers.size} vouchers")
            vouchers
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error fetching vouchers: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getVoucherById(id: String): Voucher? {
        return try {
            Log.d("FirestoreVoucherRepository", "Fetching voucher by ID: $id")
            val document = firestore.collection("vouchers")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                val voucher = document.toObject(Voucher::class.java)?.copy(id = document.id)
                Log.d("FirestoreVoucherRepository", "Successfully fetched voucher: ${voucher?.title}")
                voucher
            } else {
                Log.d("FirestoreVoucherRepository", "Voucher not found with ID: $id")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error fetching voucher by ID: ${e.message}", e)
            null
        }
    }

    override suspend fun getVoucherByCode(code: String): Voucher? {
        return try {
            Log.d("FirestoreVoucherRepository", "Fetching voucher by code: $code")
            val snapshot = firestore.collection("vouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val voucher = document.toObject(Voucher::class.java)?.copy(id = document.id)
                Log.d("FirestoreVoucherRepository", "Successfully fetched voucher by code: ${voucher?.title}")
                voucher
            } else {
                Log.d("FirestoreVoucherRepository", "Voucher not found with code: $code")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error fetching voucher by code: ${e.message}", e)
            null
        }
    }

    override suspend fun createVoucher(voucher: Voucher): String {
        val data = voucherToMap(voucher)
        val docRef = firestore.collection("vouchers")
            .add(data)
            .await()
        return docRef.id
    }

    override suspend fun updateVoucher(voucher: Voucher) {
        val docRef = firestore.collection("vouchers").document(voucher.id)
        val data = voucherToMap(voucher)
        docRef.set(data, SetOptions.merge()).await()
    }

    private fun voucherToMap(voucher: Voucher): Map<String, Any?> {
        val result = mapOf(
            "name" to voucher.id,
            "code" to voucher.code,
            "title" to voucher.title,
            "description" to voucher.description,
            "type" to voucher.type.name,
            "value" to voucher.value,
            "imageUrl" to voucher.imageUrl,
            "status" to voucher.status.name,
            "validFrom" to voucher.validFrom,
            "validTo" to voucher.validTo,
            "isStackable" to voucher.isStackable,
            "minBookingAmount" to voucher.minBookingAmount,
            "maxDiscountAmount" to voucher.maxDiscountAmount,
            "maxUsagePerUser" to voucher.maxUsagePerUser,
            "maxTotalUsage" to voucher.maxTotalUsage,
            "minNights" to voucher.minNights,
            "requiredUserLevel" to voucher.requiredUserLevel,
            "validDays" to voucher.validDays,
            "validTimeSlots" to voucher.validTimeSlots,
            "applyForHotel" to voucher.applyForHotel,
            "createdAt" to voucher.createdAt
        )
        return result
    }

    // Claim methods
    override suspend fun claimVoucher(voucherId: String, userId: String): Boolean {
        return try {
            Log.d("FirestoreVoucherRepository", "Claiming voucher: $voucherId for user: $userId")
            
            // Check if already claimed
            val isAlreadyClaimed = isVoucherClaimed(voucherId, userId)
            if (isAlreadyClaimed) {
                Log.d("FirestoreVoucherRepository", "Voucher already claimed by user")
                return false
            }
            
            // Create claim record
            val claimData = mapOf(
                "voucherId" to voucherId,
                "userId" to userId,
                "claimedAt" to Date(),
                "createdAt" to Date()
            )
            
            firestore.collection("voucher_claims").add(claimData).await()
            Log.d("FirestoreVoucherRepository", "Successfully claimed voucher: $voucherId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error claiming voucher: ${e.message}", e)
            false
        }
    }

    override suspend fun isVoucherClaimed(voucherId: String, userId: String): Boolean {
        return try {
            Log.d("FirestoreVoucherRepository", "Checking if voucher claimed: $voucherId by user: $userId")
            val snapshot = firestore.collection("voucher_claims")
                .whereEqualTo("voucherId", voucherId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val isClaimed = !snapshot.isEmpty
            Log.d("FirestoreVoucherRepository", "Voucher claim status: $isClaimed")
            isClaimed
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w("FirestoreVoucherRepository", "Permission denied checking voucher claim status - assuming not claimed for graceful fallback")
                    // Graceful fallback: assume not claimed to allow claiming
                    false
                }
                else -> {
                    Log.e("FirestoreVoucherRepository", "Firestore error checking voucher claim status: ${e.message}", e)
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error checking voucher claim status: ${e.message}", e)
            false
        }
    }

    // Eligibility methods
    override suspend fun checkVoucherEligibility(voucherId: String, userId: String): Pair<Boolean, String> {
        return try {
            Log.d("FirestoreVoucherRepository", "Checking eligibility for voucher: $voucherId, user: $userId")
            
            // Get voucher
            val voucher = getVoucherById(voucherId)
            if (voucher == null) {
                return Pair(false, "Voucher not found")
            }
            
            // Check if already claimed
            val isClaimed = isVoucherClaimed(voucherId, userId)
            if (isClaimed) {
                return Pair(false, "Voucher already claimed")
            }
            
            // Check validity period
            val now = Date()
            if (voucher.validFrom.toDate().after(now)) {
                return Pair(false, "Voucher not yet valid")
            }
            if (voucher.validTo.toDate().before(now)) {
                return Pair(false, "Voucher has expired")
            }
            
            // Check status
            if (voucher.status != com.example.chillstay.domain.model.VoucherStatus.ACTIVE) {
                return Pair(false, "Voucher is not active")
            }
            
            // Check usage limits with PERMISSION_DENIED handling
            if (voucher.maxTotalUsage > 0) {
                try {
                    val totalClaimsSnapshot = firestore.collection("voucher_claims")
                        .whereEqualTo("voucherId", voucherId)
                        .get()
                        .await()
                    
                    if (totalClaimsSnapshot.documents.size >= voucher.maxTotalUsage) {
                        return Pair(false, "Voucher usage limit reached")
                    }
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("FirestoreVoucherRepository", "Permission denied checking total usage - skipping limit check for graceful fallback")
                        // Skip usage limit check, allow claiming
                    } else {
                        throw e
                    }
                }
            }
            
            // Check per-user usage limit with PERMISSION_DENIED handling
            if (voucher.maxUsagePerUser > 0) {
                try {
                    val userClaimsSnapshot = firestore.collection("voucher_claims")
                        .whereEqualTo("voucherId", voucherId)
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                    
                    if (userClaimsSnapshot.documents.size >= voucher.maxUsagePerUser) {
                        return Pair(false, "You have reached the usage limit for this voucher")
                    }
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("FirestoreVoucherRepository", "Permission denied checking user usage - skipping limit check for graceful fallback")
                        // Skip usage limit check, allow claiming
                    } else {
                        throw e
                    }
                }
            }
            
            Log.d("FirestoreVoucherRepository", "User is eligible for voucher")
            Pair(true, "You are eligible to claim this voucher")
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w("FirestoreVoucherRepository", "Permission denied checking eligibility - returning graceful fallback")
                    // Graceful fallback: assume eligible with manual check message
                    Pair(true, "Check manually - Try claiming anyway")
                }
                else -> {
                    Log.e("FirestoreVoucherRepository", "Firestore error checking eligibility: ${e.message}", e)
                    Pair(false, "Unable to check eligibility")
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error checking eligibility: ${e.message}", e)
            Pair(false, "Unable to check eligibility")
        }
    }

    override suspend fun applyVoucherToHotels(voucherId: String, hotelIds: List<String>): Boolean {
        try {
            val docRef = firestore.collection("vouchers").document(voucherId)
            firestore.runTransaction { tx ->
                val snap = tx.get(docRef)
                if (!snap.exists()) throw IllegalStateException("Voucher not found")

                val current = snap.get("applyForHotel") as? List<*> ?: emptyList<Any>()
                val currentIds = current.filterIsInstance<String>()
                val merged = (currentIds + hotelIds).distinct()

                tx.update(docRef, "applyForHotel", merged)
                true
            }.await()
            Log.d("FirestoreVoucherRepository", "Voucher $voucherId updated applyForHotel -> merged ${hotelIds.size} items")
            return true
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreVoucherRepository", "FirestoreException while applying voucher: ${e.code} ${e.message}", e)
            return false
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Exception while applying voucher: ${e.message}", e)
            return false
        }
    }

    // Method 2: updateVoucherAppliedHotels
    override suspend fun updateVoucherAppliedHotels(
        voucherId: String,
        hotelIds: List<String>
    ): Boolean {
        return try {
            Log.d(ACCOM_TAG, "=== updateVoucherAppliedHotels START ===")
            Log.d(ACCOM_TAG, "voucherId: $voucherId")
            Log.d(ACCOM_TAG, "New hotelIds to add: $hotelIds (count: ${hotelIds.size})")

            // Get current voucher
            Log.d(ACCOM_TAG, "Fetching current voucher...")
            val voucher = getVoucherById(voucherId)
            if (voucher == null) {
                Log.e(ACCOM_TAG, "❌ Voucher not found: $voucherId")
                return false
            }

            Log.d(ACCOM_TAG, "✅ Voucher found: ${voucher.title}")

            // Merge new hotel IDs with existing ones
            val currentHotelIds = voucher.applyForHotel ?: emptyList()
            Log.d(ACCOM_TAG, "Current applyForHotel: $currentHotelIds (count: ${currentHotelIds.size})")

            val updatedHotelIds = (currentHotelIds + hotelIds).distinct()
            Log.d(ACCOM_TAG, "Updated applyForHotel: $updatedHotelIds (count: ${updatedHotelIds.size})")

            // Update voucher document
            val updates = mapOf(
                "applyForHotel" to updatedHotelIds
            )

            Log.d(ACCOM_TAG, "Updating voucher document in Firestore...")
            Log.d(ACCOM_TAG, "Document path: vouchers/$voucherId")
            Log.d(ACCOM_TAG, "Updates: $updates")

            firestore.collection("vouchers")
                .document(voucherId)
                .set(updates, SetOptions.merge())
                .await()

            Log.d(ACCOM_TAG, "✅ Voucher document updated successfully")

            // Verify update
            Log.d(ACCOM_TAG, "Verifying update...")
            val updatedVoucher = getVoucherById(voucherId)
            val verifiedIds = updatedVoucher?.applyForHotel ?: emptyList()
            Log.d(ACCOM_TAG, "Verified applyForHotel after update: $verifiedIds (count: ${verifiedIds.size})")

            if (verifiedIds.size == updatedHotelIds.size) {
                Log.d(ACCOM_TAG, "✅ Verification successful!")
            } else {
                Log.w(ACCOM_TAG, "⚠️ Verification mismatch: expected ${updatedHotelIds.size}, got ${verifiedIds.size}")
            }

            Log.d(ACCOM_TAG, "=== updateVoucherAppliedHotels END (SUCCESS) ===")
            true
        } catch (e: Exception) {
            Log.e(ACCOM_TAG, "❌ Error in updateVoucherAppliedHotels: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
}
