package com.example.chillstay.domain.model

data class Room(
    val id: String = "",
    val hotelId: String = "",
    val name: String = "",
    val area: Double = 0.0,
    val doubleBed : Int = 0,
    val singleBed : Int = 0,
    val quantity : Int = 0,
    val feature: List<String> = emptyList(),
    val breakfastPrice: Double = 0.0,
    val price: Double = 0.0,
    val discount: Double = 0.0,
    val capacity: Int = 0,
    val gallery: RoomGallery? = null,
    val status : RoomStatus = RoomStatus.ACTIVE,
) {
    // Firestore mapping helper
    constructor() : this(
        id = "",
        hotelId = "",
        name = "",
        area = 0.0,
        doubleBed = 0,
        singleBed = 0,
        quantity = 0,
        feature = emptyList(),
        breakfastPrice = 0.0,
        price = 0.0,
        discount = 0.0,
        capacity = 0,
        gallery = null,
        status = RoomStatus.ACTIVE
    )
}

enum class RoomStatus {
    ACTIVE,
    INACTIVE
}

data class RoomGallery(
    val exteriorView: List<String> = emptyList(),
    val dining: List<String> = emptyList(),
    val thisRoom: List<String> = emptyList()
) {
    val totalCount: Int
        get() = exteriorView.size + dining.size + thisRoom.size
}


