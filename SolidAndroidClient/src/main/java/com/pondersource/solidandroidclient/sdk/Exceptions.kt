package com.pondersource.solidandroidclient.sdk

import com.pondersource.shared.domain.error.ExceptionsErrorCode
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

fun handleSolidException(errorCode: Int, errorMessage: String): SolidException {
    return when (errorCode) {
        ExceptionsErrorCode.DRAW_OVERLAY_NOT_PERMITTED -> SolidServicesDrawPermissionDeniedException(errorMessage)
        ExceptionsErrorCode.SOLID_NOT_LOGGED_IN -> SolidNotLoggedInException(errorMessage)
        ExceptionsErrorCode.NOT_SUPPORTED_CLASS -> SolidResourceException.NotSupportedClassException(errorMessage)
        ExceptionsErrorCode.NOT_PERMISSION -> SolidResourceException.NotPermissionException(errorMessage)
        ExceptionsErrorCode.NULL_WEBID -> SolidResourceException.NullWebIdException(errorMessage)
        else -> SolidResourceException.UnknownException(errorMessage)
    }
}