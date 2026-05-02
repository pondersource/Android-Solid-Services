package com.pondersource.shared.domain.datamodule

import android.os.Parcelable

public sealed class DataModuleResult<T : Parcelable> {
    public data class Success<T : Parcelable>(val data: T) : DataModuleResult<T>()
    public data class Error<T : Parcelable>(val errorMessage: String?) : DataModuleResult<T>()
    public data class Exception<T : Parcelable>(val exception: Throwable) : DataModuleResult<T>()
}

public fun <T : Parcelable> DataModuleResult<T>.getOrThrow(): T = when (this) {
    is DataModuleResult.Success -> data
    is DataModuleResult.Error -> throw IllegalStateException(errorMessage)
    is DataModuleResult.Exception -> throw exception
}

public fun <T : Parcelable> DataModuleResult<T>.getOrNull(): T? =
    if (this is DataModuleResult.Success) data else null

