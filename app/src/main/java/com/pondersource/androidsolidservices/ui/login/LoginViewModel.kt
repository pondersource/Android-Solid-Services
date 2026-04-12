package com.pondersource.androidsolidservices.ui.login

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.ui.navigation.Login
import com.pondersource.solidandroidapi.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val authenticator: Authenticator,
    savedStateHandle: SavedStateHandle,
): BaseViewModel() {

    val isAddingAccount: Boolean = savedStateHandle.toRoute<Login>().isAddingAccount

    companion object {
        private const val AUTH_APP_REDIRECT_URL = "com.pondersource.androidsolidservices:/oauth2redirect"
        private const val OIDC_ISSUER_INRUPT_COM = "https://login.inrupt.com"
        private const val OIDC_ISSUER_SOLIDCOMMIUNITY = "https://solidcommunity.net"
    }

    val loginBrowserIntent = mutableStateOf<Intent?>(null)
    val loginBrowserIntentErrorMessage = mutableStateOf<String?>(null)
    val loginLoading = mutableStateOf(false)
    val loginResult = mutableStateOf(false)

    private fun launchLogin(block: suspend () -> Pair<Intent?, String?>) {
        viewModelScope.launch {
            loginLoading.value = true
            try {
                val intentRes = block()
                loginBrowserIntentErrorMessage.value = intentRes.second
                loginBrowserIntent.value = intentRes.first
                if (intentRes.first == null) {
                    loginLoading.value = false
                }
            } catch (e: Exception) {
                loginBrowserIntentErrorMessage.value = e.message ?: "Login failed"
                loginLoading.value = false
            }
        }
    }

    fun loginWithWebId(webId: String) {
        launchLogin {
            authenticator.createAuthenticationIntentWithWebId(webId, AUTH_APP_REDIRECT_URL)
        }
    }

    fun loginWithInruptCom() {
        launchLogin {
            authenticator.createAuthenticationIntentWithOidcIssuer(OIDC_ISSUER_INRUPT_COM, AUTH_APP_REDIRECT_URL)
        }
    }

    fun loginWithSolidcommunity() {
        launchLogin {
            authenticator.createAuthenticationIntentWithOidcIssuer(OIDC_ISSUER_SOLIDCOMMIUNITY, AUTH_APP_REDIRECT_URL)
        }
    }

    fun loginWithCustomIssuer(issuerUrl: String) {
        launchLogin {
            authenticator.createAuthenticationIntentWithOidcIssuer(issuerUrl, AUTH_APP_REDIRECT_URL)
        }
    }

    fun submitAuthorizationResponse(
        authorizationResponse: AuthorizationResponse?,
        authorizationException: AuthorizationException?
    ) {
        viewModelScope.launch {
            try {
                authenticator.submitAuthorizationResponse(
                    authorizationResponse,
                    authorizationException
                )
            } catch (_: Exception) {
                // Token exchange may have succeeded even if a later step (e.g. WebID
                // profile fetch) failed. Fall through to the isLoggedIn() check.
            }
            loginLoading.value = false
            loginBrowserIntent.value = null
            if (isLoggedIn()) {
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
        // getActiveWebId() awaits Authenticator init, ensuring profiles are loaded.
        authenticator.getActiveWebId()
        return authenticator.isUserAuthorized()
    }
}