package com.example.chillstay.data.repository

import com.example.chillstay.domain.model.SampleItem
import com.example.chillstay.domain.repository.SampleRepository

class InMemorySampleRepository : SampleRepository {
    override suspend fun getItems(): List<SampleItem> = listOf(
        SampleItem(id = "1", title = "Xin chào"),
        SampleItem(id = "2", title = "MVVM Skeleton"),
        SampleItem(id = "3", title = "Bắt đầu coding")
    )
}
