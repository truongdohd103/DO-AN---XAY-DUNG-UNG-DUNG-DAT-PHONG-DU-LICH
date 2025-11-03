package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.domain.repository.VipStatusRepository

class UpdateVipStatusUseCase(
    private val vipStatusRepository: VipStatusRepository
) {
    suspend operator fun invoke(vipStatus: VipStatus): Result<VipStatus> {
        return try {
            val updated = vipStatusRepository.updateVipStatus(vipStatus)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



