package com.example.chillstay.di

import com.example.chillstay.domain.usecase.GetSampleItems
import com.example.chillstay.domain.usecase.GetHotelsUseCase
import com.example.chillstay.domain.usecase.SearchHotelsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetSampleItems(get()) }
    factory { GetHotelsUseCase(get()) }
    factory { SearchHotelsUseCase(get()) }
}
