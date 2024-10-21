package com.pondersource.solidandroidclient

import android.content.Intent
import com.pondersource.solidandroidclient.data.Profile
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.TokenResponse

interface Authenticator {

    /**
     * @param webId user's webId
     * @param redirectUri your application's redirect URL which you declared in your app.
     * This parameter should be set correctly to be able to return to your application after doing the authentication phase in a browser on the phone.
     * @return A pair of intent and an error message. The intent should be used to start the authentication in a browser.
     * In case it is null then the error message will contain the reason. in which can be showed to user.
     */
    suspend fun createAuthenticationIntentWithWebId(
        webId: String,
        redirectUri: String,
    ) : Pair<Intent?, String?>

    /**
     * @param oidcIssuer Identity Issuer url
     * @param redirectUri your application's redirect URL which you declared in your app.
     * This parameter should be set correctly to be able to return to your application after doing the authentication phase in a browser on the phone.
     * @return A pair of intent and an error message. The intent should be used to start the authentication in a browser.
     * In case it is null then the error message will contain the reason. in which can be showed to user.
     */
    suspend fun createAuthenticationIntentWithOidcIssuer(
        oidcIssuer: String,
        redirectUri: String,
    ) : Pair<Intent?, String?>

    /**
     * After using the intent (createAuthenticationIntentWithOidcIssuer, createAuthenticationIntentWithWebId) in the browser and completing the authentication, you need to call this method with the returned data from browser.
     * @param authResponse can be created from returned intent
     * @param authException can be created from returned intent
     */
    suspend fun submitAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    )

    /**
     * @return TokenResponse of the user which can be used to make authenticated requests
     */
    suspend fun getLastTokenResponse(): TokenResponse?

    /**
     * Checks if the user token needs to be refreshed
     * @return bool
     */
    fun needsTokenRefresh(): Boolean

    /**
     * Checks if the user is authorized
     * @return bool
     */
    fun isUserAuthorized(): Boolean

    /**
     * @return The user's profile such as AuthState, WebId and WebIdDetails
     */
    fun getProfile(): Profile

    /**
     * clears saved user's data. Can be used after logout in your application
     */
    fun resetProfile()

    suspend fun getTerminationSessionIntent(
        logoutRedirectUrl: String,
    ) : Pair<Intent?, String?>
}