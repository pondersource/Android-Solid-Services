package com.pondersource.solidandroidapi

import android.content.Context
import android.content.Intent
import com.pondersource.shared.data.Profile
import kotlinx.coroutines.flow.StateFlow
import net.openid.appauth.TokenResponse

/**
 * Handles OpenID Connect authentication with Solid identity providers using DPoP
 * (Demonstration of Proof-of-Possession).
 *
 * Obtain an instance via [Authenticator.getInstance].  All suspend functions are safe
 * to call from any coroutine context; they internally dispatch onto IO threads.
 */
interface Authenticator {

    companion object {
        /**
         * Returns the application-scoped singleton [Authenticator].
         * @param context Any [Context]; the application context is used internally.
         */
        fun getInstance(context: Context): Authenticator = AuthenticatorImplementation.getInstance(context)
    }

    /**
     * Hot [StateFlow] that emits whenever the active [Profile] changes — e.g. after login,
     * logout, or an account switch.  Always holds the latest value; never completes.
     */
    val activeProfileFlow: StateFlow<Profile>

    /**
     * Builds an authorization [Intent] by first resolving the OIDC issuer from the user's
     * WebID document.
     * @param webId The user's WebID URI.
     * @param redirectUri The URI that the identity provider should redirect to after login.
     * @return A [Pair] of (intent, error message).  The intent is `null` on failure.
     */
    suspend fun createAuthenticationIntentWithWebId(
        webId: String,
        redirectUri: String,
    ) : Pair<Intent?, String?>

    /**
     * Builds an authorization [Intent] using a known OIDC issuer URL directly,
     * bypassing WebID discovery.
     * @param oidcIssuer The OIDC issuer base URL (e.g. `https://solidcommunity.net`).
     * @param redirectUri The URI that the identity provider should redirect to after login.
     * @return A [Pair] of (intent, error message).  The intent is `null` on failure.
     */
    suspend fun createAuthenticationIntentWithOidcIssuer(
        oidcIssuer: String,
        redirectUri: String,
    ) : Pair<Intent?, String?>

    /**
     * Completes the authorization code exchange after the browser redirects back to the app.
     * @param authResponse The successful authorization response, or `null` on failure.
     * @param authException The authorization exception, or `null` on success.
     * @return The authenticated WebID on success, or `null` on failure.
     */
    suspend fun submitAuthorizationResponse(
        authResponse: net.openid.appauth.AuthorizationResponse?,
        authException: net.openid.appauth.AuthorizationException?
    ): String?

    /**
     * Returns a valid access token for [webId], refreshing it automatically if expired.
     * @param webId The WebID whose token should be retrieved.
     * @param forceRefresh If `true`, always refresh the token even if it is still valid.
     * @return The [TokenResponse], or `null` if the user is not authenticated.
     */
    suspend fun getLastTokenResponse(
        webId: String,
        forceRefresh: Boolean = false
    ): TokenResponse?

    /**
     * Builds the HTTP authorization headers (Authorization + DPoP) needed for a
     * resource request on behalf of [webId].
     * @param webId The WebID making the request.
     * @param httpMethod The HTTP verb (e.g. `"GET"`, `"PUT"`).
     * @param uri The full resource URI being requested.
     * @return A map of header name → value ready to add to the request.
     */
    suspend fun getAuthHeaders(
        webId: String,
        httpMethod: String,
        uri: String
    ): Map<String, String>

    /**
     * Updates the cached DPoP nonce for [webId] from a server-issued `DPoP-Nonce` header.
     * Call this whenever the server returns a fresh nonce.
     */
    fun updateDPoPNonce(
        webId: String,
        nonce: String,
    )

    /** Returns `true` if at least one account has completed authentication. */
    fun isUserAuthorized(): Boolean

    /** Returns all profiles that have completed authentication. */
    fun getAllLoggedInProfiles(): List<Profile>

    /**
     * Returns the [Profile] for a specific [webId].
     * @throws NoSuchElementException if no profile exists for the given WebID.
     */
    fun getProfile(webId: String): Profile

    /**
     * Returns the active (currently selected) profile.
     * Falls back to the first authorized profile if no active account is set.
     * @throws NoSuchElementException if no authorized profiles exist.
     */
    fun getProfile(): Profile

    /** Returns the WebID of the currently active account, or `null` if none is set. */
    suspend fun getActiveWebId(): String?

    /**
     * Switches the active account to [webId].
     * Emits the updated profile on [activeProfileFlow].
     */
    suspend fun setActiveWebId(webId: String)

    /** Removes the active account's profile and tokens from local storage. */
    suspend fun resetProfile()

    /**
     * Removes the profile and tokens for the given [webId] from local storage.
     * If [webId] was the active account, a new active account is chosen automatically.
     */
    suspend fun resetProfile(webId: String)

    /**
     * Builds an end-session [Intent] to log the user out at the identity provider.
     * @param webId The WebID of the account to sign out.
     * @param logoutRedirectUrl The URI to redirect to after the IdP logs the user out.
     * @return A [Pair] of (intent, error message).  The intent is `null` on failure.
     */
    suspend fun getTerminationSessionIntent(
        webId: String,
        logoutRedirectUrl: String,
    ) : Pair<Intent?, String?>
}