package com.example.chillstay.domain.usecase.image

import android.net.Uri
import com.example.chillstay.domain.repository.ImageUploadRepository

class UploadVoucherImageUseCase(private val imageUploadRepository: ImageUploadRepository) {
    suspend operator fun invoke(
        voucherId: String,
        imageUri: Uri
    ): String {
        return imageUploadRepository.uploadVoucherImage(
            voucherId = voucherId,
            imageUri = imageUri
        )
    }
}