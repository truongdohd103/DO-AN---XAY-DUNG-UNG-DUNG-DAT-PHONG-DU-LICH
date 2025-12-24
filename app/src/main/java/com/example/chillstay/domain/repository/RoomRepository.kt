package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Room

interface RoomRepository {
    suspend fun getRoomsByHotelId(
        hotelId: String,
        checkIn: String? = null,
        checkOut: String? = null,
        guests: Int? = null
    ): List<Room>
    suspend fun getRoomById(roomId: String): Room?
    suspend fun reserveRoomUnits(roomId: String, count: Int): Boolean
    suspend fun releaseRoomUnits(roomId: String, count: Int): Boolean

    suspend fun createRoom(room: Room): String
    suspend fun updateRoom(room: Room)
}