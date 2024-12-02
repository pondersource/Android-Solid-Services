package com.pondersource.shared.data.datamodule

import android.os.Parcelable

sealed class DataModuleResult<T: Parcelable> {

    data class Success<T: Parcelable>(val data: T) : DataModuleResult<T>()
    data class Error<T: Parcelable>(val errorMessage: String?) : DataModuleResult<T>()
}

fun <T: Parcelable> DataModuleResult<T>.extractResult(): T? {
    return when(this) {
        is DataModuleResult.Success -> {
            this.data
        }
        is DataModuleResult.Error -> {
            null
        }
    }
}