package com.example.chillstay.data.repository.storage

import android.net.Uri
import com.example.chillstay.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadImage(uri: Uri, folder: String): String {
        return try {
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("$folder/$filename")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }
}
