package com.example.chillstay.di

import com.example.chillstay.domain.usecase.GetSampleItems
import com.example.chillstay.domain.usecase.GetHotelsUseCase
import com.example.chillstay.domain.usecase.SearchHotelsUseCase
import com.example.chillstay.domain.usecase.SignUpUseCase
import com.example.chillstay.domain.usecase.SignInUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetSampleItems(get()) }
    factory { GetHotelsUseCase(get()) }
    factory { SearchHotelsUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { SignInUseCase(get()) }
}
