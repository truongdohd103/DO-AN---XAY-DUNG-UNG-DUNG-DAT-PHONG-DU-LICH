package com.example.chillstay.domain.model

import com.google.firebase.firestore.PropertyName

data class Room(
    val id: String = "",
    val hotelId: String = "",
    val type: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val detail: RoomDetail? = null,
    @get:PropertyName("isAvailable")
    val isAvailable: Boolean = true,
    val capacity: Int = 0
) {
    // Firestore mapping helper
    constructor() : this(
        id = "",
        hotelId = "",
        type = "",
        price = 0.0,
        imageUrl = "",
        detail = null,
        isAvailable = true,
        capacity = 0
    )
}


