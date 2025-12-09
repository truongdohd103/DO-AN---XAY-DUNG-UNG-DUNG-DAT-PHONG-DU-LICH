package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.domain.model.VipStatusHistory
import com.example.chillstay.domain.repository.VipStatusRepository
import com.example.chillstay.core.common.Result

class GetVipStatusHistoryUseCase constructor(
    private val vipStatusRepository: VipStatusRepository
) {
    suspend operator fun invoke(userId: String): Result<List<VipStatusHistory>> {
        return try {
            val history = vipStatusRepository.getVipStatusHistory(userId)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

