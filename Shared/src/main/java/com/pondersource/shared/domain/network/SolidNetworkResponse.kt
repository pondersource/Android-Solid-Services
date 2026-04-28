package com.pondersource.shared.domain.network

sealed class SolidNetworkResponse<T> {
    data class Success<T>(val data: T) : SolidNetworkResponse<T>()
    data class Error<T>(val errorCode: Int, val errorMessage: String) : SolidNetworkResponse<T>()
    data class Exception<T>(val exception: Throwable) : SolidNetworkResponse<T>()

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw IllegalStateException("Operation failed ($errorCode): $errorMessage")
        is Exception -> throw exception
    }

    fun getOrNull(): T? = if (this is Success) data else null

    fun getOrDefault(default: T): T = if (this is Success) data else default
}
