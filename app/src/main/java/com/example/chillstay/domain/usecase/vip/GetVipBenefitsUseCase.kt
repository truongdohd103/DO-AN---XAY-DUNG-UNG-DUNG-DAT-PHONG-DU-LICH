package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.domain.model.VipBenefit
import com.example.chillstay.domain.model.VipLevel
import com.example.chillstay.domain.repository.VipStatusRepository
import com.example.chillstay.core.common.Result

class GetVipBenefitsUseCase constructor(
    private val vipStatusRepository: VipStatusRepository
) {
    suspend operator fun invoke(level: VipLevel): Result<List<VipBenefit>> {
        return try {
            val benefits = vipStatusRepository.getVipBenefits(level)
            Result.success(benefits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

