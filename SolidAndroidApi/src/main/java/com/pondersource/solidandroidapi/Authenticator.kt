package com.pondersource.solidandroidapi

import android.content.Intent
import com.pondersource.shared.data.Profile

interface Authenticator {

    suspend fun createAuthenticationIntentWithWebId(
        webId: String,
        redirectUri: String,
    ) : Pair<Intent?, String?>

    suspend fun createAuthenticationIntentWithOidcIssuer(
        oidcIssuer: String,
        redirectUri: String,
    ) : Pair<Intent?, String?>

    suspend fun submitAuthorizationResponse(
        authResponse: net.openid.appauth.AuthorizationResponse?,
        authException: net.openid.appauth.AuthorizationException?
    )

    suspend fun getLastTokenResponse(forceRefresh: Boolean = false): net.openid.appauth.TokenResponse?

    suspend fun getAuthHeaders(httpMethod: String, uri: String): Map<String, String>

    fun isUserAuthorized(): Boolean

    fun getProfile(): Profile

    fun resetProfile()

    suspend fun getTerminationSessionIntent(
        logoutRedirectUrl: String,
    ) : Pair<Intent?, String?>
}