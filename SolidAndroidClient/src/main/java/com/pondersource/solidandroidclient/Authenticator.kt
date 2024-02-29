package com.pondersource.solidandroidclient

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.inrupt.client.auth.Session
import com.inrupt.client.solid.SolidSyncClient
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.RegistrationRequest
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenResponse
import java.net.URI


class Authenticator {

    companion object {
        @Volatile
        private lateinit var INSTANCE: Authenticator

        fun getInstance(context: Context, authState: AuthState? = null): Authenticator {
            return if (::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = if (authState == null) {
                    Authenticator(context)
                } else {
                    Authenticator(context, authState)
                }
                INSTANCE
            }
        }
    }

    private val authService: AuthorizationService
    private var authState: AuthState

    private constructor(context: Context) : this(context, AuthState())

    private constructor(context: Context, authState: AuthState) {
        this.authService = AuthorizationService(context)
        this.authState = authState
    }

    fun getAuthState() = authState

    fun isLoggedIn(): Boolean {
        return authState.isAuthorized
    }

    fun needsTokenRefresh(): Boolean {
        return authState.needsTokenRefresh
    }

    fun createAuthenticationIntentWithWebId(
        webId: String,
        redirectUri: String,
        callback: (response: Intent?, errorMessage: String?) -> Unit
    ) {
        val webIdDetails = getWebIdDetails(webId)

        createAuthenticationIntentWithOidcIssuer(webIdDetails.oidcIssuer.id, redirectUri, callback)
    }

    fun createAuthenticationIntentWithOidcIssuer(
        oidcIssuer: String,
        redirectUri: String,
        callback: (response: Intent?, errorMessage: String?) -> Unit
    ) {

        AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(oidcIssuer)) { serviceConfiguration, exception ->
            if (serviceConfiguration != null) {

                registerToOpenId(
                    serviceConfiguration,
                    redirectUri
                ) { response, ex ->

                    authState.update(response)

                    if (response != null) {
                        val builder = AuthorizationRequest.Builder(
                            serviceConfiguration,
                            response.clientId,
                            ResponseTypeValues.CODE,
                            Uri.parse(redirectUri))

                        val authRequest = builder
                            .setScope("openid +offline_access")
                            .setPrompt("consent")
                            .setResponseMode(AuthorizationRequest.ResponseMode.QUERY)
                            .build()

                        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
                        callback(authIntent, null)
                    }
                    else {
                        callback(null, "can not register to OpenId")
                    }
                }
            }
            else {
               callback(null, "can not get access to web-id issuer configurations.")
            }
        }
    }

    fun requestToken() {
        requestToken{ tokenResponse, authorizationException ->
            //DO NOTHING
        }
    }

    fun requestToken(
        callback: (TokenResponse?, AuthorizationException?) -> (Unit)
    ) {
        if (authState.lastAuthorizationResponse != null) {
            authService.performTokenRequest(
                authState.lastAuthorizationResponse!!.createTokenExchangeRequest(),
                ClientSecretBasic(authState.lastRegistrationResponse!!.clientSecret!!)
            ) { tokenResponse, exception ->

                updateTokenResponse(tokenResponse, exception)
                callback(tokenResponse, exception)
            }
        } else {
            callback(null, authState.authorizationException)
        }
    }

    fun updateAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        authState.update(authResponse, authException)
    }

    fun updateTokenResponse(
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ) {
        authState.update(tokenResponse, authException)
    }

    fun getToken(
        callback: (String?, String?, AuthorizationException?) -> (Unit)
    ) {
        authState.performActionWithFreshTokens(
            authService,
            ClientSecretBasic(authState.lastRegistrationResponse!!.clientSecret!!),
            callback
        )
    }

    fun terminateSession(
        logoutRedirectUrl: String,
        callback: (endSessionIntent: Intent?, errorMessage: String?) -> (Unit)
    ) {

        if (authState.lastAuthorizationResponse != null &&
            authState.authorizationServiceConfiguration != null) {

            getToken { accessToken, idToken, ex ->
                if (idToken != null) {
                    val endSessionReq = EndSessionRequest.Builder(authState.authorizationServiceConfiguration!!)
                        .setIdTokenHint(idToken)
                        .setPostLogoutRedirectUri(Uri.parse(logoutRedirectUrl))
                        .build()
                    callback(authService.getEndSessionRequestIntent(endSessionReq), null)
                }
                else {
                    callback(null, ex?.message ?: "Problem with refreshing token.")
                }
            }
        } else {
            callback(null, "There is no configuration")
        }
    }


    private fun getWebIdDetails(webId: String): WebId {
        val client = SolidSyncClient
            .getClient()
            .session(Session.anonymous())

        return client.get(URI(webId), WebId::class.java)
    }

    private fun registerToOpenId(
        conf: AuthorizationServiceConfiguration,
        redirectUri: String,
        callback: (response: RegistrationResponse?, ex: AuthorizationException?) -> Unit
    ) {
        val regReq = RegistrationRequest.Builder(
            conf,
            listOf(Uri.parse(redirectUri))
        ).setAdditionalParameters(mapOf(
            "client_name" to "Android Solid Services",
            //"id_token_signed_response_alg" to "ES256",
            //"id_token_signed_response_alg" to "RS256",
            "id_token_signed_response_alg" to conf.discoveryDoc!!.idTokenSigningAlgorithmValuesSupported[0],
        ))
            .setSubjectType("public")
            .setTokenEndpointAuthenticationMethod("client_secret_basic")
            .setGrantTypeValues(listOf("authorization_code", "refresh_token"))
            .build()

        authService.performRegistrationRequest(regReq) { response, ex ->
            callback(response, ex)
        }
    }

    fun resetAuthState() {
        authState = AuthState()
    }
}