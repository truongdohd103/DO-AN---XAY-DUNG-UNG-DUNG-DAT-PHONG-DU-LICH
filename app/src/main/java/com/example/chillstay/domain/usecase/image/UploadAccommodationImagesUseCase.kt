package com.example.chillstay.domain.usecase.image

import android.net.Uri
import com.example.chillstay.domain.repository.ImageUploadRepository

class UploadAccommodationImagesUseCase(
    private val repository: ImageUploadRepository
) {
    suspend operator fun invoke(
        hotelId: String,
        imageUris: List<Uri>
    ): List<String> {
        return repository.uploadAccommodationImages(
            hotelId = hotelId,
            imageUris = imageUris
        )
    }
}


