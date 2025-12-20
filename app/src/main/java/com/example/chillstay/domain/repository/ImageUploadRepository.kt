package com.example.chillstay.domain.repository

import android.net.Uri

interface ImageUploadRepository {
    suspend fun uploadAccommodationImages(
        hotelId: String,
        accommodationName: String,
        imageUris: List<Uri>
    ): List<String>
}


