package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.VipStatusHistory
import com.example.chillstay.domain.repository.VipStatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetVipStatusHistoryUseCase @Inject constructor(
    private val vipStatusRepository: VipStatusRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<VipStatusHistory>>> = flow {
        try {
            val history = vipStatusRepository.getVipStatusHistory(userId)
            emit(Result.Success(history))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}
