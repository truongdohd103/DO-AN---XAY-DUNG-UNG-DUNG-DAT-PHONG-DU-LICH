package com.example.chillstay.ui.search

import com.example.chillstay.core.base.UiEvent

sealed interface SearchUiEvent : UiEvent {
    data class QueryChanged(val value: String) : SearchUiEvent
    data class CountryChanged(val value: String) : SearchUiEvent
    data class CityChanged(val value: String) : SearchUiEvent
    data class MinRatingChanged(val value: String) : SearchUiEvent
    data class MaxPriceChanged(val value: String) : SearchUiEvent
    object Submit : SearchUiEvent
    object ClearFilters : SearchUiEvent
}


