package com.example.chillstay.domain.usecase.image

import android.net.Uri
import com.example.chillstay.domain.repository.ImageUploadRepository

class UploadRoomImagesUseCase(
    private val repository: ImageUploadRepository
) {
    suspend operator fun invoke(
        roomId: String,
        hotelId: String,
        tag: String,
        imageUris: List<Uri>
    ): List<String> {
        return repository.uploadRoomImages(
            roomId = roomId,
            hotelId = hotelId,
            tag = tag,
            imageUris = imageUris
        )
    }
}