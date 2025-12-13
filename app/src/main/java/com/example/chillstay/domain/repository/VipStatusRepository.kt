package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.domain.model.VipStatusHistory
import com.example.chillstay.domain.model.VipBenefit

interface VipStatusRepository {
    suspend fun getVipStatus(userId: String): VipStatus?
    suspend fun createVipStatus(vipStatus: VipStatus): VipStatus
    suspend fun updateVipStatus(vipStatus: VipStatus): VipStatus
    suspend fun addPoints(userId: String, points: Int, reason: String, bookingId: String? = null): Boolean
    suspend fun redeemPoints(userId: String, points: Int, reason: String): Boolean
    suspend fun getVipStatusHistory(userId: String): List<VipStatusHistory>
    suspend fun getVipBenefits(level: com.example.chillstay.domain.model.VipLevel): List<VipBenefit>
    suspend fun calculateVipLevel(points: Int): com.example.chillstay.domain.model.VipLevel
    suspend fun getNextLevelPoints(currentLevel: com.example.chillstay.domain.model.VipLevel): Int
    suspend fun addVipStatusHistory(history: VipStatusHistory)
}

