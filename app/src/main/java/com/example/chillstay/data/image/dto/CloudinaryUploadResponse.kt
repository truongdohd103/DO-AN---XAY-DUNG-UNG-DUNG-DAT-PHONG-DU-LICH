package com.example.chillstay.data.image.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudinaryUploadResponse(
    @SerialName("public_id")
    val publicId: String,

    @SerialName("version")
    val version: Long,

    @SerialName("signature")
    val signature: String,

    @SerialName("width")
    val width: Int,

    @SerialName("height")
    val height: Int,

    @SerialName("format")
    val format: String,

    @SerialName("resource_type")
    val resourceType: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("bytes")
    val bytes: Long,

    @SerialName("type")
    val type: String,

    @SerialName("url")
    val url: String,

    @SerialName("secure_url")
    val secureUrl: String
)