package com.pondersource.solidandroidclient.sdk

sealed class SolidException(message: String): Exception(message) {
    class SolidServiceConnectionException: SolidException("")
    class SolidAppNotFoundException: SolidException("")
    class SolidNotLoggedInException: SolidException("")

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