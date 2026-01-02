package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.domain.repository.VipStatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetVipStatusUseCase @Inject constructor(
    private val vipStatusRepository: VipStatusRepository
) {
    operator fun invoke(userId: String): Flow<Result<VipStatus?>> = flow {
        try {
            val vipStatus = vipStatusRepository.getVipStatus(userId)
            emit(Result.Success(vipStatus))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}

