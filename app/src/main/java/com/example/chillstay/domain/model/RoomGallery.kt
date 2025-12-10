package com.example.chillstay.domain.model

data class RoomGallery(
    val exteriorView: List<String> = emptyList(),
    val facilities: List<String> = emptyList(),
    val dining: List<String> = emptyList(),
    val thisRoom: List<String> = emptyList()
) {
    val totalCount: Int
        get() = exteriorView.size + facilities.size + dining.size + thisRoom.size
}

