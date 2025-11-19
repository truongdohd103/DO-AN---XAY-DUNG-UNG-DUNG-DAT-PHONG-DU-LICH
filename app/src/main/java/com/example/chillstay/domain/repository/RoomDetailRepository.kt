package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.RoomDetail

interface RoomDetailRepository {
    suspend fun getRoomDetail(roomId: String): RoomDetail?
    suspend fun createRoomDetail(roomDetail: RoomDetail): RoomDetail
    suspend fun updateRoomDetail(roomDetail: RoomDetail): RoomDetail
    suspend fun deleteRoomDetail(roomId: String): Boolean
}






