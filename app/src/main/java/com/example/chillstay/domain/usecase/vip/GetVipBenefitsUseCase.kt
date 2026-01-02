package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.VipBenefit
import com.example.chillstay.domain.model.VipLevel
import com.example.chillstay.domain.repository.VipStatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetVipBenefitsUseCase @Inject constructor(
    private val vipStatusRepository: VipStatusRepository
) {
    operator fun invoke(level: VipLevel): Flow<Result<List<VipBenefit>>> = flow {
        try {
            val benefits = vipStatusRepository.getVipBenefits(level)
            emit(Result.Success(benefits))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}
