package com.pondersource.solidandroidapi

import android.content.Context
import android.content.Intent
import com.pondersource.shared.data.Profile
import kotlinx.coroutines.flow.StateFlow
import net.openid.appauth.TokenResponse

interface Authenticator {

    companion object {
        fun getInstance(context: Context): Authenticator = AuthenticatorImplementation.getInstance(context)
    }

    val activeProfileFlow: StateFlow<Profile>

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
    ): String?

    suspend fun getLastTokenResponse(
        webId: String,
        forceRefresh: Boolean = false
    ): TokenResponse?

    suspend fun getAuthHeaders(
        webId: String,
        httpMethod: String,
        uri: String
    ): Map<String, String>

    fun updateDPoPNonce(
        webId: String,
        nonce: String,
    )

    fun isUserAuthorized(): Boolean

    fun getAllLoggedInProfiles(): List<Profile>

    fun getProfile(
        webId: String,
    ): Profile

    /**
     * Returns the active (currently selected) profile.
     * Falls back to the first authorized profile if no active account is set.
     * @throws NoSuchElementException if no authorized profiles exist.
     */
    fun getProfile(): Profile

    suspend fun getActiveWebId(): String?

    suspend fun setActiveWebId(webId: String)

    suspend fun resetProfile()

    suspend fun resetProfile(
        webId: String,
    )

    suspend fun getTerminationSessionIntent(
        webId: String,
        logoutRedirectUrl: String,
    ) : Pair<Intent?, String?>
}