package com.erfangholami.androidsolidservices.shared.domain.network

public sealed class SolidNetworkResponse<T> {
    public data class Success<T>(val data: T) : SolidNetworkResponse<T>()
    public data class Error<T>(val errorCode: Int, val errorMessage: String) : SolidNetworkResponse<T>()
    public data class Exception<T>(val exception: Throwable) : SolidNetworkResponse<T>()

    public fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw IllegalStateException("Operation failed ($errorCode): $errorMessage")
        is Exception -> throw exception
    }

    public fun getOrNull(): T? = if (this is Success) data else null

    public fun getOrDefault(default: T): T = if (this is Success) data else default
}
