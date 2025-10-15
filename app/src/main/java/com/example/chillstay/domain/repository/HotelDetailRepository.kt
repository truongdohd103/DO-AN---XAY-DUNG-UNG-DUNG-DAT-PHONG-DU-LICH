package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.HotelDetail
import com.example.chillstay.domain.model.RoomDetail

interface HotelDetailRepository {
    suspend fun getHotelDetail(hotelId: String): HotelDetail?
    suspend fun createHotelDetail(hotelDetail: HotelDetail): HotelDetail
    suspend fun updateHotelDetail(hotelDetail: HotelDetail): HotelDetail
    suspend fun deleteHotelDetail(hotelId: String): Boolean
}

interface RoomDetailRepository {
    suspend fun getRoomDetail(roomId: String): RoomDetail?
    suspend fun createRoomDetail(roomDetail: RoomDetail): RoomDetail
    suspend fun updateRoomDetail(roomDetail: RoomDetail): RoomDetail
    suspend fun deleteRoomDetail(roomId: String): Boolean
}






