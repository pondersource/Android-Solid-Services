package com.pondersource.androidsolidservices

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pondersource.solidandroidclient.Authenticator
import com.pondersource.solidandroidclient.AuthenticatorImplementation
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse


class AuthViewModel private constructor(context: Context): ViewModel() {

    companion object {

        private const val AUTH_APP_REDIRECT_URL = "com.pondersource.androidsolidservices:/oauth2redirect"
        private const val AUTH_END_REDIRECT_URL = "com.pondersource.androidsolidservices:/oauth2logout"
        private const val OIDC_ISSUER_INRUPT_COM = "https://login.inrupt.com"
        private const val OIDC_ISSUER_SOLIDCOMMIUNITY = "https://solidcommunity.net"


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
    }

    private val authenticator : Authenticator = AuthenticatorImplementation.getInstance(context)

    val loginBrowserIntent: MutableLiveData<Intent?> = MutableLiveData(null)
    val loginBrowserIntentErrorMessage: MutableLiveData<String?> = MutableLiveData(null)
    val loginLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val loginResult: MutableLiveData<Boolean> = MutableLiveData(false)

    fun loginWithWebId(
        webId: String,
    ) {
        viewModelScope.launch {
            loginLoading.postValue(true)
            val intentRes = authenticator.createAuthenticationIntentWithWebId(webId, AUTH_APP_REDIRECT_URL)
            loginBrowserIntentErrorMessage.postValue(intentRes.second)
            loginBrowserIntent.postValue(intentRes.first)
        }
    }

    fun loginWithInruptCom() {
        viewModelScope.launch {
            loginLoading.postValue(true)
            val intentRes = authenticator.createAuthenticationIntentWithOidcIssuer(OIDC_ISSUER_INRUPT_COM, AUTH_APP_REDIRECT_URL,)
            loginBrowserIntentErrorMessage.postValue(intentRes.second)
            loginBrowserIntent.postValue(intentRes.first)
        }
    }

    fun loginWithSolidcommunity() {
        viewModelScope.launch {
            loginLoading.postValue(true)
            val intentRes = authenticator.createAuthenticationIntentWithOidcIssuer(
                OIDC_ISSUER_SOLIDCOMMIUNITY, AUTH_APP_REDIRECT_URL,)
            loginBrowserIntentErrorMessage.postValue(intentRes.second)
            loginBrowserIntent.postValue(intentRes.first)
        }
    }

    fun submitAuthorizationResponse(
        authorizationResponse: AuthorizationResponse?,
        authorizationException: AuthorizationException?
    ) {
        viewModelScope.launch {
            authenticator.submitAuthorizationResponse(authorizationResponse, authorizationException)
            loginLoading.postValue(false)
            loginBrowserIntent.postValue(null)
            if(isLoggedIn()) {
                loginBrowserIntentErrorMessage.postValue(null)
                loginResult.postValue(true)
            } else {
                loginResult.postValue(false)
                if (authorizationException != null) {
                    loginBrowserIntentErrorMessage.postValue(authorizationException.errorDescription)
                } else {
                    loginBrowserIntentErrorMessage.postValue("A problem during login occurred!")
                }
            }
        }
    }


    fun logoutWithBrowser() {
        viewModelScope.launch {
            authenticator.getTerminationSessionIntent(AUTH_END_REDIRECT_URL)
        }
    }

    fun logout() {
        authenticator.resetProfile()
    }

    suspend fun isLoggedIn(): Boolean {
        return if(authenticator.isUserAuthorized()) {
            authenticator.getLastTokenResponse()
            true
        } else {
            false
        }
    }

    fun getProfile() = authenticator.getProfile()
}