package com.example.chillstay.data.repository.image

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.chillstay.data.image.config.CloudinaryConfig
import com.example.chillstay.data.image.dto.CloudinaryUploadResponse
import com.example.chillstay.domain.repository.ImageUploadRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.ByteArrayOutputStream
import java.text.Normalizer

class ImageUploadRepositoryImpl(
    private val httpClient: HttpClient,
    private val context: Context
) : ImageUploadRepository {

    companion object {
        private const val LOG_TAG = "ChillStayImageUpload"
        private const val UPLOAD_PRESET = "ml_default" // ensure exact
        private const val REQUEST_TIMEOUT_MS: Long = 120_000
        private const val CONNECT_TIMEOUT_MS: Long = 60_000
        private const val SOCKET_TIMEOUT_MS: Long = 120_000
        private const val COMPRESS_THRESHOLD_BYTES = 500 * 1024 // 500 KB
        private const val MAX_COMPRESS_WIDTH = 1280
        private const val DEFAULT_QUALITY = 80
    }

    override suspend fun uploadAccommodationImages(
        hotelId: String,
        accommodationName: String,
        imageUris: List<Uri>
    ): List<String> {
        if (imageUris.isEmpty()) {
            Log.d(LOG_TAG, "No images to upload")
            return emptyList()
        }

        Log.d(LOG_TAG, "Starting upload: hotelId=$hotelId, name='$accommodationName', count=${imageUris.size}")

        val folderName = slugify(accommodationName.ifBlank { hotelId })
        val folder = "${CloudinaryConfig.BASE_FOLDER}/$folderName"

        Log.d(LOG_TAG, "Upload folder: $folder")

        return coroutineScope {
            imageUris.mapIndexed { index, uri ->
                async {
                    uploadSingleImageUnsigned(uri, hotelId, folder, index)
                }
            }.awaitAll().filterNotNull()
        }
    }

    private suspend fun uploadSingleImageUnsigned(
        uri: Uri,
        hotelId: String,
        folder: String,
        index: Int
    ): String? {
        return try {
            val fileName = queryFileName(uri) ?: "image_$index.jpg"
            val contentTypeString = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = prepareBytesForUpload(uri)
            Log.d(LOG_TAG, "[$index] Uploading (UNSIGNED): $fileName (${bytes.size} bytes)")

            val timestamp = System.currentTimeMillis()
            val publicId = "${hotelId}_${timestamp}_$index"

            Log.d(LOG_TAG, "[$index] Upload parameters (UNSIGNED):")
            Log.d(LOG_TAG, "  - URL: ${CloudinaryConfig.getUploadUrl()}")
            Log.d(LOG_TAG, "  - Upload Preset: $UPLOAD_PRESET")
            Log.d(LOG_TAG, "  - Folder: $folder")
            Log.d(LOG_TAG, "  - Public ID: $publicId")

            // Build multipart with explicit Content-Disposition for text parts AND file part
            val multipart = formData {
                // Text parts with explicit headers
                append(
                    key = "upload_preset",
                    value = UPLOAD_PRESET,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"upload_preset\"")
                        append(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                    }
                )

                append(
                    key = "folder",
                    value = folder,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"folder\"")
                        append(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                    }
                )

                append(
                    key = "public_id",
                    value = publicId,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"public_id\"")
                        append(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                    }
                )

                // File part
                append(
                    key = "file",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$fileName\"")
                        append(HttpHeaders.ContentType, contentTypeString)
                    }
                )
            }

            val response: HttpResponse = httpClient.post(CloudinaryConfig.getUploadUrl()) {
                setBody(MultiPartFormDataContent(multipart))
                timeout {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MS
                    connectTimeoutMillis = CONNECT_TIMEOUT_MS
                    socketTimeoutMillis = SOCKET_TIMEOUT_MS
                }
            }

            Log.d(LOG_TAG, "[$index] Response status: ${response.status.value} ${response.status.description}")

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Log.e(LOG_TAG, "[$index] Upload failed with status ${response.status.value}")
                Log.e(LOG_TAG, "[$index] Error response: $errorBody")
                return null
            }

            val cloudinaryResponse = response.body<CloudinaryUploadResponse>()
            Log.d(LOG_TAG, "[$index] Upload SUCCESS!")
            Log.d(LOG_TAG, "[$index] Secure URL: ${cloudinaryResponse.secureUrl}")

            cloudinaryResponse.secureUrl

        } catch (e: Exception) {
            Log.e(LOG_TAG, "[$index] Upload FAILED for $uri", e)
            val msg = e.message ?: "no message"
            Log.e(LOG_TAG, "[$index] Exception message: $msg")
            null
        }
    }

    private fun queryFileName(uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Cannot get file name from URI: ${e.message}")
            null
        } finally {
            cursor?.close()
        }
    }

    private fun slugify(input: String): String {
        if (input.isEmpty()) return "unnamed"
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        val withoutAccents = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return withoutAccents.lowercase()
            .replace("[^a-z0-9]+".toRegex(), "-")
            .replace("^-+|-+$".toRegex(), "")
    }

    private fun prepareBytesForUpload(uri: Uri): ByteArray {
        val input = context.contentResolver.openInputStream(uri) ?: throw IllegalStateException("Cannot open $uri")
        val raw = input.use { it.readBytes() }
        return if (raw.size > COMPRESS_THRESHOLD_BYTES) {
            compressImageUri(uri, MAX_COMPRESS_WIDTH, DEFAULT_QUALITY)
        } else {
            raw
        }
    }

    private fun compressImageUri(uri: Uri, maxWidth: Int, quality: Int): ByteArray {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

        val origW = options.outWidth.takeIf { it > 0 } ?: return context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
        val scale = (origW.toFloat() / maxWidth).coerceAtLeast(1f)
        val sampleSize = scale.toInt().coerceAtLeast(1)

        val opts2 = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts2) }
            ?: throw IllegalStateException("Cannot decode bitmap for $uri")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val bytes = baos.toByteArray()
        bitmap.recycle()
        return bytes
    }
}
