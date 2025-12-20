package com.example.chillstay.domain.usecase.image

import android.net.Uri
import com.example.chillstay.domain.repository.ImageUploadRepository

class UploadAccommodationImagesUseCase(
    private val repository: ImageUploadRepository
) {
    suspend operator fun invoke(
        hotelId: String,
        accommodationName: String,
        imageUris: List<Uri>
    ): List<String> {
        return repository.uploadAccommodationImages(
            hotelId = hotelId,
            accommodationName = accommodationName,
            imageUris = imageUris
        )
    }
}


