package com.pondersource.shared.domain.datamodule

import android.os.Parcelable

sealed class DataModuleResult<T : Parcelable> {
    data class Success<T : Parcelable>(val data: T) : DataModuleResult<T>()
    data class Error<T : Parcelable>(val errorMessage: String?) : DataModuleResult<T>()
    data class Exception<T : Parcelable>(val exception: Throwable) : DataModuleResult<T>()
}

fun <T : Parcelable> DataModuleResult<T>.getOrThrow(): T = when (this) {
    is DataModuleResult.Success -> data
    is DataModuleResult.Error -> throw IllegalStateException(errorMessage)
    is DataModuleResult.Exception -> throw exception
}

fun <T : Parcelable> DataModuleResult<T>.getOrNull(): T? = if (this is DataModuleResult.Success) data else null

