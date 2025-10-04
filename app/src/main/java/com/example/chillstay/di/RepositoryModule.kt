package com.example.chillstay.di

import com.example.chillstay.data.repository.InMemorySampleRepository
import com.example.chillstay.data.repository.InMemoryHotelRepository
import com.example.chillstay.domain.repository.SampleRepository
import com.example.chillstay.domain.repository.HotelRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SampleRepository> { InMemorySampleRepository() }
    single<HotelRepository> { InMemoryHotelRepository() }
}
