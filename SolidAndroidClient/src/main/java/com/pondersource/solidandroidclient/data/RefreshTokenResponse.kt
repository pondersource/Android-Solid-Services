package com.pondersource.solidandroidclient.data

import net.openid.appauth.AuthorizationException

data class RefreshTokenResponse(
    val accessToken: String?,
    val idToken: String?,
    val exception: AuthorizationException?,
)
