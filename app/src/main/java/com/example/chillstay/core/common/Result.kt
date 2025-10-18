package com.example.chillstay.core.common

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun failure(throwable: Throwable): Result<Nothing> = Error(throwable)
    }
}
