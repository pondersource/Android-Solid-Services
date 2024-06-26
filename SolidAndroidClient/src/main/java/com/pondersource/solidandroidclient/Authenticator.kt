package com.pondersource.solidandroidclient

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.inrupt.client.auth.Session
import com.inrupt.client.solid.SolidSyncClient
import com.pondersource.solidandroidclient.data.Profile
import com.pondersource.solidandroidclient.data.RefreshTokenResponse
import com.pondersource.solidandroidclient.data.UserInfo
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authenticator is responsible to do the Authentication phase with the selected Identity Provider.
 */
class Authenticator {

    companion object {

        private const val SHARED_PREFERENCES_NAME = "solid_android_auth"
        private const val PROFILE_STATE_KEY = "profile_state"
        private const val PROFILE_USER_INFO_KEY = "profile_user_info"
        private const val PROFILE_WEB_ID_DETAILS_KEY = "profile_web_id_details"

        @Volatile
        private lateinit var INSTANCE: Authenticator

        /**
         *  get a single instance of the class
         *  @param context ApplicationContext
         *  @return Authenticator object
         */
        fun getInstance(context: Context): Authenticator {
            return if (::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = Authenticator(context)
                INSTANCE
            }
        }
    }

    private var sharedPreferences: SharedPreferences
    private val authService: AuthorizationService
    private var profile: Profile

    private constructor(context: Context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        this.profile = readProfileFromCache()
        this.authService = AuthorizationService(context)
    }

    private fun readProfileFromCache(): Profile {
        val profile = Profile()
        val stateString = sharedPreferences.getString(PROFILE_STATE_KEY, null)
        if (!stateString.isNullOrEmpty()) {
            profile.authState =  AuthState.jsonDeserialize(stateString)
        }
        profile.userInfo = Gson().fromJson(sharedPreferences.getString(PROFILE_USER_INFO_KEY, null), UserInfo::class.java)
        profile.webIdDetails = Gson().fromJson(sharedPreferences.getString(
            PROFILE_WEB_ID_DETAILS_KEY, null), WebId::class.java)

        return profile
    }

    private fun writeProfileToCache() {
        sharedPreferences.edit().apply {
            putString(PROFILE_STATE_KEY, profile.authState.jsonSerializeString())
            putString(PROFILE_USER_INFO_KEY, Gson().toJson(profile.userInfo))
            putString(PROFILE_WEB_ID_DETAILS_KEY, Gson().toJson(profile.webIdDetails))
            apply()
        }
    }

    /**
     * @return AuthState of the user
     */
    fun getAuthState() = profile.authState

    /**
     * @return The user's profile such as AuthState, WebId and WebIdDetails
     */
    fun getProfile() = profile

    /**
     * Checks if the user is authorized
     * @return bool
     */

    fun isUserAuthorized(): Boolean {
        return profile.authState.isAuthorized
    }

    /**
     * Checks if the user token needs to be refreshed
     * @return bool
     */
    fun needsTokenRefresh(): Boolean {
        return profile.authState.needsTokenRefresh
    }

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
    ) : Pair<Intent?, String?> {
        val webIdDetails = getWebIdDetails(webId)

        return createAuthenticationIntentWithOidcIssuer(webIdDetails.oidcIssuer!!.id!!, redirectUri)
    }

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
    ) : Pair<Intent?, String?> {

        val conf = getAuthorizationConf(oidcIssuer)

        return if (conf.first != null) {

            registerToOpenId(conf.first!!, redirectUri)

            if (profile.authState.lastRegistrationResponse != null) {
                val builder = AuthorizationRequest.Builder(
                    conf.first!!,
                    profile.authState.lastRegistrationResponse!!.clientId,
                    ResponseTypeValues.CODE,
                    Uri.parse(redirectUri))

                val authRequest = builder
                    .setScopes( "webid", "openid", "offline_access",)
                    .setPrompt("consent")
                    .setResponseMode(AuthorizationRequest.ResponseMode.QUERY)
                    .build()

                val authIntent = authService.getAuthorizationRequestIntent(authRequest)
                Pair(authIntent, null)
            }
            else {
                Pair(null, "can not register to OpenId")
            }
        } else {
            Pair(null, "can not get access to web-id issuer configurations.")
        }
    }

    /**
     * Refreshes the token in case it has been expired.
     * @return RefreshTokenResponse which contains accessToken, tokenId and exception
     */
    suspend fun refreshToken(): RefreshTokenResponse {
        return suspendCoroutine { cont ->
            profile.authState.performActionWithFreshTokens(
                authService,
                ClientSecretBasic(profile.authState.lastRegistrationResponse!!.clientSecret!!)
            ) { accessToken, idToken, ex ->
                cont.resume(RefreshTokenResponse(accessToken, idToken, ex))
            }
        }
    }

    private fun getWebIdDetails(webId: String): WebId {
        val client = SolidSyncClient
            .getClient()
            .session(Session.anonymous())

        return client.get(URI(webId), WebId::class.java)
    }

    private fun getStorage(webId: String): String {
        if (webId.contains("solidcommunity.net")) {
            return webId.split("profile")[0]
        }
        return webId
    }

    private suspend fun getAuthorizationConf(
        oidcIssuer: String
    ): Pair<AuthorizationServiceConfiguration?, AuthorizationException?>{
        return suspendCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(oidcIssuer)) { serviceConfiguration, exception ->
                cont.resume(Pair(serviceConfiguration, exception))
            }
        }
    }

    private suspend fun registerToOpenId(
        conf: AuthorizationServiceConfiguration,
        redirectUri: String,
    ) {
        val regReq = RegistrationRequest.Builder(
            conf,
            listOf(Uri.parse(redirectUri))
        ).setAdditionalParameters(mapOf(
            "client_name" to "Android Solid Services",
            "id_token_signed_response_alg" to conf.discoveryDoc!!.idTokenSigningAlgorithmValuesSupported[0],
        ))
            .setSubjectType("public")
            .setTokenEndpointAuthenticationMethod("client_secret_basic")
            .setGrantTypeValues(listOf("authorization_code", "refresh_token"))
            .build()

        val res = suspendCoroutine { cont ->
            authService.performRegistrationRequest(regReq) { response, ex ->
                cont.resume(response)
            }
        }
        updateRegistrationResponse(res)
    }

    /**
     * After using the intent (createAuthenticationIntentWithOidcIssuer, createAuthenticationIntentWithWebId) in the browser and completing the authentication, you need to call this method with the returned data from browser.
     * @param authResponse can be created from returned intent
     * @param authException can be created from returned intent
     */
    suspend fun submitAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        updateAuthorizationResponse(authResponse, authException)
        if (authException == null && authResponse != null) {
            requestToken()
            profile.userInfo = getUserInfo()
            profile.webIdDetails = getWebIdDetails(profile.userInfo!!.webId)
            if (profile.webIdDetails == null) {
                profile.webIdDetails = WebId()
            }
            if (profile.webIdDetails!!.storage == null) {
                profile.webIdDetails!!.storage = JObject(getStorage(profile.userInfo!!.webId))
            }
            writeProfileToCache()
        }
    }

    private suspend fun checkAuthenticator(): Boolean {
        return if (isUserAuthorized()) {
            if (!needsTokenRefresh()) {
                true
            } else {
                //Need to refresh token
                val tokenRes = refreshToken()
                profile.authState.idToken != null
            }
        } else {
            //false
            val tokenRes = refreshToken()
            profile.authState.idToken != null
        }
    }

    private suspend fun getUserInfo(): UserInfo? {
        val isUserAuthenticated = checkAuthenticator()

        if (isUserAuthenticated){
            val webId = Utils.getWebId(profile.authState.idToken)
            return UserInfo(webId)
        } else {
            return null
        }
    }

    private suspend fun requestToken(): Pair<TokenResponse?, AuthorizationException?> {

        val result : Pair<TokenResponse?, AuthorizationException?> = if (profile.authState.lastAuthorizationResponse != null) {
            suspendCoroutine { cont ->
                authService.performTokenRequest(
                    profile.authState.lastAuthorizationResponse!!.createTokenExchangeRequest(),
                    ClientSecretBasic(profile.authState.lastRegistrationResponse!!.clientSecret!!)
                ) { tokenResponse, exception ->
                    cont.resume(Pair(tokenResponse, exception))
                }
            }
        } else {
            Pair(null, profile.authState.authorizationException)
        }
        updateTokenResponse(result.first, result.second)
        return result
    }

    suspend fun getTerminationSessionIntent(
        logoutRedirectUrl: String,
    ) : Pair<Intent?, String?>{

        return if (profile.authState.lastAuthorizationResponse != null &&
            profile.authState.authorizationServiceConfiguration != null) {

            val tokenRes = refreshToken()

            if (tokenRes.idToken != null) {
                val endSessionReq = EndSessionRequest.Builder(profile.authState.authorizationServiceConfiguration!!)
                    .setIdTokenHint(tokenRes.idToken)
                    .setPostLogoutRedirectUri(Uri.parse(logoutRedirectUrl))
                    .build()
                Pair(authService.getEndSessionRequestIntent(endSessionReq), null)
            }
            else {
                Pair(null, tokenRes.exception?.message ?: "Problem with refreshing token.")
            }

        } else {
            Pair(null, "There is no configuration")
        }
    }

    private fun updateRegistrationResponse(
        regResponse: RegistrationResponse?
    ) {
        profile.authState.update(regResponse)
    }

    private fun updateAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        profile.authState.update(authResponse, authException)
    }

    private fun updateTokenResponse(
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ) {
        profile.authState.update(tokenResponse, authException)
    }

    /**
     * clears saved user's data. Can be used after logout in your application
     */
    fun resetProfile() {
        profile = Profile()
        writeProfileToCache()
    }
}
