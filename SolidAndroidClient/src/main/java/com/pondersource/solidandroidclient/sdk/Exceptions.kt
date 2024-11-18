package com.pondersource.solidandroidclient.sdk

import com.pondersource.solidandroidclient.sdk.SolidException.SolidNotLoggedInException
import com.pondersource.solidandroidclient.sdk.SolidException.SolidResourceException
import com.pondersource.solidandroidclient.sdk.SolidException.SolidServicesDrawPermissionDeniedException

sealed class SolidException(message: String): Exception(message) {
    class SolidServicesDrawPermissionDeniedException(message: String = "Android Solid Services doesn't have permission to draw overlay."): SolidException(message)
    class SolidServiceConnectionException(message: String = "Unable to connect to Android Solid Services."): SolidException(message)
    class SolidAppNotFoundException(message: String = "Android Solid Services has not been installed."): SolidException(message)
    class SolidNotLoggedInException(message: String = "User has not logged in."): SolidException(message)

    sealed class SolidResourceException(message: String): SolidException(message) {
        class NotSupportedClassException(message: String): SolidResourceException(message)
        class NotPermissionException(message: String): SolidResourceException(message)
        class NullWebIdException(message: String = "WebID is missing!"): SolidResourceException(message)
        class UnknownException(message: String): SolidResourceException(message)
    }
}

object ExceptionsErrorCode {

    const val DRAW_OVERLAY_NOT_PERMITTED = 1
    const val SOLID_NOT_LOGGED_IN = 2

    const val NOT_SUPPORTED_CLASS = 100
    const val NOT_PERMISSION = 101
    const val NULL_WEBID = 102
    const val UNKNOWN = 103
}

fun handleSolidResourceException(errorCode: Int, errorMessage: String): SolidResourceException {
    return when (errorCode) {
        ExceptionsErrorCode.NOT_SUPPORTED_CLASS -> {
            SolidResourceException.NotSupportedClassException(errorMessage)
        }
        ExceptionsErrorCode.NOT_PERMISSION -> {
            SolidResourceException.NotPermissionException(errorMessage)
        }
        ExceptionsErrorCode.NULL_WEBID -> {
            SolidResourceException.NullWebIdException(errorMessage)
        }
        else -> {
            SolidResourceException.UnknownException(errorMessage)
        }
    }
}

fun handleSolidException(errorCode: Int, errorMessage: String): SolidException {
    return when (errorCode) {
        ExceptionsErrorCode.DRAW_OVERLAY_NOT_PERMITTED -> {
            SolidServicesDrawPermissionDeniedException(errorMessage)
        }
        ExceptionsErrorCode.SOLID_NOT_LOGGED_IN -> {
            SolidNotLoggedInException(errorMessage)
        }
        else -> {
            handleSolidResourceException(errorCode, errorMessage)
        }
    }
}