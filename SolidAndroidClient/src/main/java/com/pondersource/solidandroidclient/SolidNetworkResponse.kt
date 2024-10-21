package com.pondersource.solidandroidclient

sealed class SolidNetworkResponse<T> {
    data class Success<T>(val data: T) : SolidNetworkResponse<T>()
    data class Error<T>(val errorCode: Int, val errorMessage: String) : SolidNetworkResponse<T>()
    data class Exception<T>(val exception: Throwable) : SolidNetworkResponse<T>()
}