package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.domain.repository.VipStatusRepository
import com.example.chillstay.core.common.Result

class GetVipStatusUseCase constructor(
    private val vipStatusRepository: VipStatusRepository
) {
    suspend operator fun invoke(userId: String): Result<VipStatus?> {
        return try {
            val vipStatus = vipStatusRepository.getVipStatus(userId)
            Result.success(vipStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

