package com.pondersource.solidandroidclient.sdk

sealed class ASSConnectionResponse {
    data class Success(val accessIsGranted: Boolean) : ASSConnectionResponse()
    data class AppNotFound(val message: String) : ASSConnectionResponse()
    data class SolidHasNotLoggedIn(val message: String) : ASSConnectionResponse()
    data class Error(val message: String) : ASSConnectionResponse()
}