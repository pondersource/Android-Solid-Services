package com.pondersource.shared.data.datamodule

import android.os.Parcelable

sealed class DataModuleResult<T : Parcelable> {
    data class Success<T : Parcelable>(val data: T) : DataModuleResult<T>()
    data class Error<T : Parcelable>(val errorMessage: String?) : DataModuleResult<T>()

    /**
     * Wraps an unexpected [Throwable] that occurred during the operation.
     * Prefer this over [Error] when an exception is the root cause — it preserves
     * the full stack trace for debugging.
     */
    data class Exception<T : Parcelable>(val exception: Throwable) : DataModuleResult<T>()
}

/**
 * Returns the data on success, or throws otherwise.
 * - [DataModuleResult.Error] throws [IllegalStateException] with the error message.
 * - [DataModuleResult.Exception] re-throws the original [Throwable].
 */
fun <T : Parcelable> DataModuleResult<T>.getOrThrow(): T = when (this) {
    is DataModuleResult.Success -> data
    is DataModuleResult.Error -> throw IllegalStateException(errorMessage)
    is DataModuleResult.Exception -> throw exception
}

/** Returns the data on success, or `null` on [DataModuleResult.Error] or [DataModuleResult.Exception]. */
fun <T : Parcelable> DataModuleResult<T>.getOrNull(): T? = if (this is DataModuleResult.Success) data else null

@Deprecated(
    message = "Use getOrNull() instead.",
    replaceWith = ReplaceWith("getOrNull()"),
)
fun <T : Parcelable> DataModuleResult<T>.extractResult(): T? = getOrNull()
