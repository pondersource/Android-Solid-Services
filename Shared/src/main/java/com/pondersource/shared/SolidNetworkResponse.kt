package com.pondersource.shared

sealed class SolidNetworkResponse<T> {
    data class Success<T>(val data: T) : SolidNetworkResponse<T>()
    data class Error<T>(val errorCode: Int, val errorMessage: String) : SolidNetworkResponse<T>()
    data class Exception<T>(val exception: Throwable) : SolidNetworkResponse<T>()

    fun handleResponse(): T {
        return when (this) {
            is Success -> this.data
            is Error -> throw Exception("Error: ${this.errorCode} - ${this.errorMessage}")
            is Exception -> throw this.exception
        }
    }
}