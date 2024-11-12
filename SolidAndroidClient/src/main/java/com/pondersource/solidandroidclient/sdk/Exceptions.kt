package com.pondersource.solidandroidclient.sdk

import com.pondersource.solidandroidclient.sdk.SolidException.SolidNotLoggedInException
import com.pondersource.solidandroidclient.sdk.SolidException.SolidResourceException

sealed class SolidException(message: String): Exception(message) {
    class SolidServiceConnectionException(message: String = "Unable to connect to Android Solid Services."): SolidException(message)
    class SolidAppNotFoundException(message: String = "Android Solid Services has not been installed."): SolidException(message)
    class SolidNotLoggedInException(message: String = "User has not logged in."): SolidException(message)

    sealed class SolidResourceException(message: String): SolidException(message) {
        class NotSupportedClassException(message: String): SolidResourceException(message)
        class NotPermissionException(message: String): SolidResourceException(message)
        class UnknownException(message: String): SolidResourceException(message)
    }
}

object ExceptionsErrorCode {

    const val SOLID_NOT_LOGGED_IN = 1

    const val NOT_SUPPORTED_CLASS = 100
    const val NOT_PERMISSION = 101
    const val UNKNOWN = 102
}

fun handleSolidResourceException(errorCode: Int, errorMessage: String): SolidResourceException {
    return when (errorCode) {
        ExceptionsErrorCode.NOT_SUPPORTED_CLASS -> {
            SolidResourceException.NotSupportedClassException(errorMessage)
        }
        ExceptionsErrorCode.NOT_PERMISSION -> {
            SolidResourceException.NotPermissionException(errorMessage)
        }
        else -> {
            SolidResourceException.UnknownException(errorMessage)
        }
    }
}

fun handleSolidException(errorCode: Int, errorMessage: String): SolidException {
    return when (errorCode) {
        ExceptionsErrorCode.SOLID_NOT_LOGGED_IN -> {
            SolidNotLoggedInException(errorMessage)
        }
        else -> {
            handleSolidResourceException(errorCode, errorMessage)
        }
    }
}