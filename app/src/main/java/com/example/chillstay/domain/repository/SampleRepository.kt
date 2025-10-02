package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.SampleItem

interface SampleRepository {
    suspend fun getItems(): List<SampleItem>
}
