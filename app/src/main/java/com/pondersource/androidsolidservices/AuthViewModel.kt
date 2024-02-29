package com.pondersource.androidsolidservices

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.pondersource.solidandroidclient.Authenticator
import com.pondersource.solidandroidclient.SolidCRUD
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.TokenResponse


class AuthViewModel private constructor(context: Context): ViewModel() {

    companion object {

        private const val AUTH_APP_REDIRECT_URL = "com.pondersource.androidsolidservices:/oauth2redirect"
        private const val AUTH_END_REDIRECT_URL = "com.pondersource.androidsolidservices:/oauth2logout"
        private const val OIDC_ISSUER_INRUPT_COM = "https://login.inrupt.com"
        private const val OIDC_ISSUER_SOLIDCOMMIUNITY = "https://solidcommunity.net"

        private const val SHARED_PREFERENCES_NAME = "android_solid_services"
        private const val AUTH_STATE_KEY = "auth_state"

        @Volatile
        private lateinit var INSTANCE : AuthViewModel

        fun getInstance(context: Context) : AuthViewModel {
            return if (::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = AuthViewModel(context)
                INSTANCE
            }
        }

        private fun getLoggedInfo(context: Context): AuthState? {
            val sharedPreferences : SharedPreferences = context.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            val previousState = sharedPreferences.getString(AUTH_STATE_KEY, null)
            return if (previousState != null) {
                AuthState.jsonDeserialize(previousState)
            } else {
                null
            }
        }

    }

    private val authenticator : Authenticator = Authenticator.getInstance(context, getLoggedInfo(context))
    private val solidCRUD = SolidCRUD.getInstance(authenticator)
    private val sharedPreferences : SharedPreferences = context.getSharedPreferences(
        SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)


    fun login(
        webId: String,
        callback: (response: Intent?, errorMessage: String?) -> Unit
    ) {
        authenticator.createAuthenticationIntentWithWebId(
            webId,
            AUTH_APP_REDIRECT_URL,
            callback
        )
    }

    fun loginWithInruptCom(
        callback: (response: Intent?, errorMessage: String?) -> Unit
    ) {
        authenticator.createAuthenticationIntentWithOidcIssuer(
            OIDC_ISSUER_INRUPT_COM,
            AUTH_APP_REDIRECT_URL,
            callback
        )
    }

    fun loginWithSolidcommunity(
        callback: (response: Intent?, errorMessage: String?) -> Unit
    ) {
        authenticator.createAuthenticationIntentWithOidcIssuer(
            OIDC_ISSUER_SOLIDCOMMIUNITY,
            AUTH_APP_REDIRECT_URL,
            callback
        )
    }

    fun requestToken(
        authorizationResponse: AuthorizationResponse,
        authorizationException: AuthorizationException?,
        callback: (TokenResponse?, AuthorizationException?) -> (Unit)
    ) {
        authenticator.updateAuthorizationResponse(authorizationResponse, authorizationException)
        authenticator.requestToken(callback)
    }


    fun logout(callback: (endSessionIntent: Intent?, errorMessage: String?) -> (Unit)) {
        authenticator.terminateSession(AUTH_END_REDIRECT_URL, callback)
    }

    fun logout() {
        authenticator.resetAuthState()
        saveLoginInfo()
    }

    fun saveLoginInfo() {
        val authStateString = authenticator.getAuthState().jsonSerializeString()
        with(sharedPreferences.edit()) {
            putString(AUTH_STATE_KEY, authStateString)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
         if(authenticator.isLoggedIn()) {
             if (authenticator.needsTokenRefresh()) {
                 authenticator.requestToken()
             }
             return true
         } else {
             return false
         }
    }

    fun testCRUD() {
        solidCRUD.test()
    }
}