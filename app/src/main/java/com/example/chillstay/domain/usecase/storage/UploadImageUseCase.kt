package com.example.chillstay.domain.usecase.storage

import android.net.Uri
import com.example.chillstay.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UploadImageUseCase(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(uri: Uri, folder: String): Flow<Result<String>> = flow {
        val url = storageRepository.uploadImage(uri, folder)
        emit(Result.success(url))
    }.catch { e ->
        emit(Result.failure(e))
    }
}
