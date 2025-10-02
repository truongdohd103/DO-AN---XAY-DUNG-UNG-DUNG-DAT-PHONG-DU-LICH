package com.example.chillstay.domain.usecase

import com.example.chillstay.domain.model.SampleItem
import com.example.chillstay.domain.repository.SampleRepository

class GetSampleItems(
    private val repository: SampleRepository
) {
    suspend operator fun invoke(): List<SampleItem> = repository.getItems()
}
