package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.VipStatusHistory
import com.example.chillstay.domain.repository.VipStatusRepository

class AddVipStatusHistoryUseCase(
    private val vipStatusRepository: VipStatusRepository
) {
    suspend operator fun invoke(history: VipStatusHistory): Result<Unit> {
        return try {
            vipStatusRepository.addVipStatusHistory(history)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}






