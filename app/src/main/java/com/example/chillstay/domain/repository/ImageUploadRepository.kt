package com.example.chillstay.domain.repository

import android.net.Uri

interface ImageUploadRepository {
    suspend fun uploadAccommodationImages(
        hotelId: String,
        imageUris: List<Uri>
    ): List<String>

    suspend fun uploadRoomImages(
        roomId: String,
        hotelId: String,
        tag: String,
        imageUris: List<Uri>
    ): List<String>

    suspend fun deleteHotelFolder(hotelId: String): Boolean
    suspend fun deleteRoomFolder(hotelId: String, roomId: String): Boolean
}


