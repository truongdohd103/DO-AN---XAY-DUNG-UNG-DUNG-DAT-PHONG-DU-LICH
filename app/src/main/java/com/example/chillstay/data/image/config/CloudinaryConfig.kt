package com.example.chillstay.data.image.config

import android.util.Log

/**
 * Cloudinary configuration
 * Lưu ý: Trong production, nên lưu các thông tin này vào secure storage hoặc BuildConfig
 */
object CloudinaryConfig {
    const val CLOUD_NAME = "dobkrs98w"
    const val API_KEY = "549599385771782"
    const val API_SECRET = "41hZEBgxwCgsQoB_VbniVQpDx2E"

    const val BASE_FOLDER = "Chillstay/Accommodation"

    private const val LOG_TAG = "CloudinaryConfig"

    // Cloudinary upload endpoint
    fun getUploadUrl(): String {
        val url = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
        Log.d(LOG_TAG, "Upload URL: $url")
        return url
    }

    /**
     * Tạo signature để xác thực upload (signed upload)
     *
     * Theo Cloudinary docs: https://cloudinary.com/documentation/upload_images#generating_authentication_signatures
     *
     * Signature = SHA1(params_in_alphabetical_order + api_secret)
     *
     * QUAN TRỌNG:
     * - Params phải được sort theo alphabet order
     * - Chỉ include params được gửi trong request (exclude api_key, resource_type, và file)
     * - Format: "param1=value1&param2=value2&..."
     */
    fun generateSignature(timestamp: Long, folder: String, publicId: String): String {
        // Cloudinary yêu cầu params phải alphabetical order
        // Chỉ sign các params: folder, public_id, timestamp (không sign api_key)
        val params = mapOf(
            "folder" to folder,
            "public_id" to publicId,
            "timestamp" to timestamp.toString()
        )

        // Sort by key (alphabetical order)
        val sortedParams = params.toSortedMap()

        // Build string: key1=value1&key2=value2&...
        val paramsString = sortedParams.entries.joinToString("&") { "${it.key}=${it.value}" }

        // SHA1(params + api_secret)
        val signatureInput = "$paramsString$API_SECRET"
        val signature = sha1(signatureInput)

        Log.d(LOG_TAG, "Signature generation:")
        Log.d(LOG_TAG, "  - Params (sorted): $paramsString")
        Log.d(LOG_TAG, "  - Input length: ${signatureInput.length} chars")
        Log.d(LOG_TAG, "  - Signature: $signature")

        return signature
    }

    private fun sha1(input: String): String {
        val bytes = java.security.MessageDigest
            .getInstance("SHA-1")
            .digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validate configuration
     */
    fun validateConfig(): Boolean {
        val isValid = CLOUD_NAME.isNotBlank() &&
                API_KEY.isNotBlank() &&
                API_SECRET.isNotBlank()

        if (!isValid) {
            Log.e(LOG_TAG, "Invalid Cloudinary configuration!")
            Log.e(LOG_TAG, "  - CLOUD_NAME: ${if (CLOUD_NAME.isBlank()) "EMPTY" else "OK"}")
            Log.e(LOG_TAG, "  - API_KEY: ${if (API_KEY.isBlank()) "EMPTY" else "OK"}")
            Log.e(LOG_TAG, "  - API_SECRET: ${if (API_SECRET.isBlank()) "EMPTY" else "OK"}")
        } else {
            Log.d(LOG_TAG, "Cloudinary configuration is valid")
            Log.d(LOG_TAG, "  - Cloud Name: $CLOUD_NAME")
            Log.d(LOG_TAG, "  - API Key: $API_KEY")
        }

        return isValid
    }
}