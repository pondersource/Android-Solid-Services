package com.pondersource.solidandroidapi

import android.content.Context
import android.content.Intent
import com.pondersource.shared.domain.profile.Profile
import kotlinx.coroutines.flow.StateFlow
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.TokenResponse

interface Authenticator {

    companion object {
        fun getInstance(context: Context): Authenticator = AuthenticatorImplementation.getInstance(context)
    }

    val activeProfileFlow: StateFlow<Profile?>
    val loggedInProfilesFlow: StateFlow<List<Profile>>
    val isAuthorizedFlow: StateFlow<Boolean>
    val activeWebIdFlow: StateFlow<String?>

    suspend fun createAuthenticationIntent(
        webId: String? = null,
        oidcIssuer: String? = null,
        appName: String,
        redirectUri: String,
    ): Pair<Intent?, String?>

    suspend fun submitAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?,
    ): String?

    suspend fun getTerminationSessionIntent(
        webId: String,
        logoutRedirectUrl: String,
    ): Pair<Intent?, String?>

    suspend fun getLastTokenResponse(
        webId: String,
        forceRefresh: Boolean = false,
    ): TokenResponse?

    suspend fun getAuthHeaders(
        webId: String,
        httpMethod: String,
        uri: String,
    ): Map<String, String>

    fun updateDPoPNonce(webId: String, nonce: String)

    fun isUserAuthorized(): Boolean
    fun getAllLoggedInProfiles(): List<Profile>
    fun getProfile(webId: String): Profile
    fun getActiveProfile(): Profile

    suspend fun getActiveWebId(): String?
    suspend fun setActiveWebId(webId: String)
    suspend fun removeProfile(webId: String)
    suspend fun removeAllProfiles()
}
