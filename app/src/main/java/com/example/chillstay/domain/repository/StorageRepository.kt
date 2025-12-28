package com.example.chillstay.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadImage(uri: Uri, folder: String): String
}
