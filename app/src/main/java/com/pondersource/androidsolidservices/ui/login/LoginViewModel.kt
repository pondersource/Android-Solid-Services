package com.pondersource.androidsolidservices.ui.login

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.pondersource.androidsolidservices.base.Authenticator
import com.pondersource.androidsolidservices.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val authenticator : Authenticator
): BaseViewModel() {

    companion object {
        private const val AUTH_APP_REDIRECT_URL = "com.pondersource.androidsolidservices:/oauth2redirect"
        private const val OIDC_ISSUER_INRUPT_COM = "https://login.inrupt.com"
        private const val OIDC_ISSUER_SOLIDCOMMIUNITY = "https://solidcommunity.net"
    }

    val loginBrowserIntent = mutableStateOf<Intent?>(null)
    val loginBrowserIntentErrorMessage = mutableStateOf<String?>(null)
    val loginLoading = mutableStateOf(false)
    val loginResult = mutableStateOf(false)

    fun loginWithWebId(
        webId: String,
    ) {
        viewModelScope.launch {
            loginLoading.value = true
            val intentRes = authenticator.createAuthenticationIntentWithWebId(webId,
                AUTH_APP_REDIRECT_URL
            )
            loginBrowserIntentErrorMessage.value = intentRes.second
            loginBrowserIntent.value = intentRes.first
        }
    }

    fun loginWithInruptCom() {
        viewModelScope.launch {
            loginLoading.value = true
            val intentRes = authenticator.createAuthenticationIntentWithOidcIssuer(
                OIDC_ISSUER_INRUPT_COM,
                AUTH_APP_REDIRECT_URL,)
            loginBrowserIntentErrorMessage.value = intentRes.second
            loginBrowserIntent.value = intentRes.first
        }
    }

    fun loginWithSolidcommunity() {
        viewModelScope.launch {
            loginLoading.value = true
            val intentRes = authenticator.createAuthenticationIntentWithOidcIssuer(
                OIDC_ISSUER_SOLIDCOMMIUNITY,
                AUTH_APP_REDIRECT_URL,)
            loginBrowserIntentErrorMessage.value = intentRes.second
            loginBrowserIntent.value = intentRes.first
        }
    }

    fun submitAuthorizationResponse(
        authorizationResponse: AuthorizationResponse?,
        authorizationException: AuthorizationException?
    ) {
        viewModelScope.launch {
            authenticator.submitAuthorizationResponse(authorizationResponse, authorizationException)
            loginLoading.value = false
            loginBrowserIntent.value = null
            if(isLoggedIn()) {
                loginBrowserIntentErrorMessage.value = null
                loginResult.value = true
            } else {
                loginResult.value = false
                if (authorizationException != null) {
                    loginBrowserIntentErrorMessage.value = authorizationException.errorDescription
                } else {
                    loginBrowserIntentErrorMessage.value = "A problem during login occurred!"
                }
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return if(authenticator.isUserAuthorized()) {
            authenticator.getLastTokenResponse()
            true
        } else {
            false
        }
    }
}