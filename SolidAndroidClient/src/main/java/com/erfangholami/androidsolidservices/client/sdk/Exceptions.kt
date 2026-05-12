package com.erfangholami.androidsolidservices.client.sdk

import com.erfangholami.androidsolidservices.shared.domain.error.ExceptionsErrorCode
import com.erfangholami.androidsolidservices.client.sdk.SolidException.SolidNotLoggedInException
import com.erfangholami.androidsolidservices.client.sdk.SolidException.SolidResourceException
import com.erfangholami.androidsolidservices.client.sdk.SolidException.SolidServicesDrawPermissionDeniedException

public sealed class SolidException(message: String) : Exception(message) {
    public class SolidServicesDrawPermissionDeniedException(message: String = "Android Solid Services doesn't have permission to draw overlay.") :
        SolidException(message)

    public class SolidServiceConnectionException(message: String = "Unable to connect to Android Solid Services.") :
        SolidException(message)

    public class SolidAppNotFoundException(message: String = "Android Solid Services has not been installed.") :
        SolidException(message)

    public class SolidNotLoggedInException(message: String = "User has not logged in.") :
        SolidException(message)

    public sealed class SolidResourceException(message: String) : SolidException(message) {
        public class NotSupportedClassException(message: String) : SolidResourceException(message)
        public class NotPermissionException(message: String) : SolidResourceException(message)
        public class NullWebIdException(message: String = "WebID is missing!") :
            SolidResourceException(message)

        public class UnknownException(message: String) : SolidResourceException(message)
    }
}

public fun handleSolidException(errorCode: Int, errorMessage: String): SolidException {
    return when (errorCode) {
        ExceptionsErrorCode.DRAW_OVERLAY_NOT_PERMITTED -> SolidServicesDrawPermissionDeniedException(
            errorMessage
        )

        ExceptionsErrorCode.SOLID_NOT_LOGGED_IN -> SolidNotLoggedInException(errorMessage)
        ExceptionsErrorCode.NOT_SUPPORTED_CLASS -> SolidResourceException.NotSupportedClassException(
            errorMessage
        )

        ExceptionsErrorCode.NOT_PERMISSION -> SolidResourceException.NotPermissionException(
            errorMessage
        )

        ExceptionsErrorCode.NULL_WEBID -> SolidResourceException.NullWebIdException(errorMessage)
        else -> SolidResourceException.UnknownException(errorMessage)
    }
}
