package com.pondersource.shared

sealed class SolidNetworkResponse<T> {
    data class Success<T>(val data: T) : SolidNetworkResponse<T>()
    data class Error<T>(val errorCode: Int, val errorMessage: String) : SolidNetworkResponse<T>()
    data class Exception<T>(val exception: Throwable) : SolidNetworkResponse<T>()

    /**
     * Returns the data on success, or throws otherwise.
     * - [Error] throws [IllegalStateException] with the HTTP code and message.
     * - [Exception] re-throws the original [Throwable].
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw IllegalStateException("Operation failed ($errorCode): $errorMessage")
        is Exception -> throw exception
    }

    /** Returns the data on success, or `null` on [Error] or [Exception]. */
    fun getOrNull(): T? = if (this is Success) data else null

    /** Returns the data on success, or [default] on [Error] or [Exception]. */
    fun getOrDefault(default: T): T = if (this is Success) data else default

    @Deprecated(
        message = "Use getOrThrow() instead.",
        replaceWith = ReplaceWith("getOrThrow()"),
    )
    fun handleResponse(): T = getOrThrow()
}
