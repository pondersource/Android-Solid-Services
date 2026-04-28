package com.pondersource.solidandroidapi

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.pondersource.shared.domain.network.HTTPHeaderName
import com.pondersource.shared.domain.profile.Profile
import com.pondersource.shared.domain.profile.UserInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.RegistrationRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume

internal class AuthenticatorImplementation private constructor(
    context: Context,
) : Authenticator {

    companion object {

        @Volatile
        private var INSTANCE: Authenticator? = null

        fun getInstance(context: Context): Authenticator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthenticatorImplementation(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val profileManager = ProfileManager.getInstance(context)
    private val authService = AuthorizationService(context)
    private val inProgressAuth = InProgressAuthStore()
    private val webIdResolver = WebIdResolver()

    override val activeProfileFlow: StateFlow<Profile?> get() = profileManager.activeProfileFlow
    override val loggedInProfilesFlow: StateFlow<List<Profile>> get() = profileManager.loggedInProfilesFlow
    override val isAuthorizedFlow: StateFlow<Boolean> get() = profileManager.isAuthorizedFlow
    override val activeWebIdFlow: StateFlow<String?> get() = profileManager.activeWebIdFlow

    override suspend fun createAuthenticationIntent(
        webId: String?,
        oidcIssuer: String?,
        appName: String,
        redirectUri: String,
    ): Pair<Intent?, String?> {
        profileManager.awaitInit()

        val issuerUrl = when {
            oidcIssuer != null -> oidcIssuer
            webId != null -> {
                val webIdProfile = webIdResolver.resolve(
                    webIdUri = webId,
                    tokenProvider = { getInProgressTokenResponse() },
                    authHeadersProvider = { method, uri -> buildInProgressAuthHeaders(method, uri) },
                    nonceSink = { nonce -> updateInProgressDPoPNonce(nonce) },
                )
                webIdProfile.getOidcIssuers()[0].toString()
            }
            else -> return Pair(null, "Either webId or oidcIssuer must be provided.")
        }

        val (conf, confError) = fetchAuthorizationConfig(issuerUrl)
        if (conf == null) {
            return Pair(null, "Cannot get access to web-id issuer configurations: ${confError?.message}")
        }

        val regResponse = registerToOpenId(conf, appName, redirectUri)
            ?: return Pair(null, "Cannot register to OpenId.")

        // Store the registration in the in-progress auth state
        val authState = AuthState(conf)
        authState.update(regResponse)
        inProgressAuth.set(Profile(authState = authState))

        val authRequest = AuthorizationRequest.Builder(
            conf,
            regResponse.clientId,
            ResponseTypeValues.CODE,
            redirectUri.toUri(),
        )
            .setScopes("webid", "openid", "offline_access")
            .setPrompt("consent")
            .setResponseMode(AuthorizationRequest.ResponseMode.QUERY)
            .build()

        return Pair(authService.getAuthorizationRequestIntent(authRequest), null)
    }

    override suspend fun submitAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?,
    ): String? {
        profileManager.awaitInit()

        val current = inProgressAuth.get() ?: return null
        val updatedAuthState = deepCopyAuthState(current.authState)
        updatedAuthState.update(authResponse, authException)
        inProgressAuth.set(current.copy(authState = updatedAuthState))

        if (authException != null || authResponse == null) return null

        val (tokenResponse, tokenException) = requestToken(inProgressAuth.get()!!, isRefresh = false)
        if (tokenException != null || tokenResponse == null) return ""

        val updatedAfterToken = deepCopyAuthState(inProgressAuth.get()!!.authState)
        updatedAfterToken.update(tokenResponse, tokenException)
        inProgressAuth.set(inProgressAuth.get()!!.copy(authState = updatedAfterToken))

        val userInfo = getUserInfoFromIdToken(inProgressAuth.get()!!.authState.idToken!!)

        val webIdProfile = webIdResolver.resolve(
            webIdUri = userInfo.webId,
            tokenProvider = { inProgressAuth.get()!!.authState.lastTokenResponse },
            authHeadersProvider = { method, uri -> buildInProgressAuthHeaders(method, uri) },
            nonceSink = { nonce -> updateInProgressDPoPNonce(nonce) },
        )

        val finalProfile = inProgressAuth.get()!!.copy(
            userInfo = userInfo,
            webId = webIdProfile,
        )

        val realWebId = userInfo.webId
        profileManager.writeProfile(realWebId, finalProfile)
        profileManager.setActiveWebId(realWebId)
        inProgressAuth.clear()
        return realWebId
    }

    override suspend fun getTerminationSessionIntent(
        webId: String,
        logoutRedirectUrl: String,
    ): Pair<Intent?, String?> {
        profileManager.awaitInit()
        val profile = profileManager.getProfileOrNull(webId)
            ?: return Pair(null, "No profile found for $webId")

        if (profile.authState.lastAuthorizationResponse == null ||
            profile.authState.authorizationServiceConfiguration == null
        ) {
            return Pair(null, "There is no configuration")
        }

        val token = getLastTokenResponse(webId)
        val endSessionReq = EndSessionRequest.Builder(profile.authState.authorizationServiceConfiguration!!)
            .setIdTokenHint(token!!.idToken)
            .setPostLogoutRedirectUri(logoutRedirectUrl.toUri())
            .build()
        return Pair(authService.getEndSessionRequestIntent(endSessionReq), null)
    }

    override suspend fun getLastTokenResponse(
        webId: String,
        forceRefresh: Boolean,
    ): TokenResponse? {
        profileManager.awaitInit()
        val profile = profileManager.getProfileOrNull(webId) ?: return null
        checkTokenAndRefresh(webId, profile, forceRefresh)
        return profileManager.getProfileOrNull(webId)?.authState?.lastTokenResponse
    }

    override suspend fun getAuthHeaders(
        webId: String,
        httpMethod: String,
        uri: String,
    ): Map<String, String> {
        profileManager.awaitInit()
        val profile = profileManager.getProfile(webId)
        val tokenResponse = getLastTokenResponse(webId)!!
        val headers = mutableMapOf<String, String>()
        headers[HTTPHeaderName.AUTHORIZATION] = "${tokenResponse.tokenType} ${tokenResponse.accessToken}"
        if (tokenResponse.tokenType!!.lowercase() == "dpop") {
            headers[HTTPHeaderName.DPOP] = DPoPGenerator
                .getInstance(profile.authState.authorizationServiceConfiguration!!.discoveryDoc!!)
                .generateProof(httpMethod, uri, tokenResponse.accessToken)
        }
        return headers
    }

    override fun updateDPoPNonce(webId: String, nonce: String) {
        val profile = profileManager.getProfileOrNull(webId) ?: return
        val discoveryDoc = profile.authState.authorizationServiceConfiguration?.discoveryDoc
        if (discoveryDoc != null && discoveryDoc.supportsDPop()) {
            DPoPGenerator.getInstance(discoveryDoc).updateNonce(nonce)
        }
    }

    override fun isUserAuthorized(): Boolean = profileManager.isUserAuthorized()
    override fun getAllLoggedInProfiles(): List<Profile> = profileManager.getAllLoggedInProfiles()
    override fun getProfile(webId: String): Profile = profileManager.getProfile(webId)
    override fun getActiveProfile(): Profile = profileManager.getActiveProfile()

    override suspend fun getActiveWebId(): String? {
        profileManager.awaitInit()
        return profileManager.getActiveWebId()
    }

    override suspend fun setActiveWebId(webId: String) {
        profileManager.awaitInit()
        profileManager.setActiveWebId(webId)
    }

    override suspend fun removeProfile(webId: String) {
        profileManager.awaitInit()
        profileManager.removeProfile(webId)
    }

    override suspend fun removeAllProfiles() {
        profileManager.awaitInit()
        profileManager.removeAllProfiles()
    }

    private suspend fun fetchAuthorizationConfig(
        oidcIssuer: String,
    ): Pair<AuthorizationServiceConfiguration?, AuthorizationException?> {
        return suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(oidcIssuer.toUri()) { config, exception ->
                cont.resume(Pair(config, exception))
            }
        }
    }

    private suspend fun registerToOpenId(
        conf: AuthorizationServiceConfiguration,
        appName: String,
        redirectUri: String,
    ): net.openid.appauth.RegistrationResponse? {
        val regReq = RegistrationRequest.Builder(
            conf,
            listOf(redirectUri.toUri()),
        ).setAdditionalParameters(
            mapOf(
                "client_name" to appName,
                "id_token_signed_response_alg" to conf.discoveryDoc!!.idTokenSigningAlgorithmValuesSupported[0],
            )
        )
            .setSubjectType("public")
            .setTokenEndpointAuthenticationMethod("client_secret_basic")
            .setGrantTypeValues(listOf("authorization_code", "refresh_token"))
            .build()

        return suspendCancellableCoroutine { cont ->
            authService.performRegistrationRequest(regReq) { response, _ ->
                cont.resume(response)
            }
        }
    }

    private suspend fun requestToken(
        profile: Profile,
        isRefresh: Boolean,
    ): Pair<TokenResponse?, AuthorizationException?> {
        if (profile.authState.lastAuthorizationResponse == null) {
            return Pair(null, profile.authState.authorizationException)
        }

        val tokenRequest = profile.authState.createTokenRequest(isRefresh)
        val clientAuthentication = if (profile.authState.authorizationServiceConfiguration!!.discoveryDoc!!.supportsDPop()) {
            DPopClientSecretBasic(
                clientSecret = profile.authState.lastRegistrationResponse!!.clientSecret!!,
                configuration = tokenRequest.configuration,
                refreshToken = profile.authState.refreshToken,
            )
        } else {
            ClientSecretBasic(profile.authState.lastRegistrationResponse!!.clientSecret!!)
        }

        return suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(
                tokenRequest,
                clientAuthentication
            ) { tokenResponse, exception ->
                cont.resume(Pair(tokenResponse, exception))
            }
        }
    }

    private suspend fun checkTokenAndRefresh(
        webId: String,
        profile: Profile,
        forceRefresh: Boolean = false,
    ) {
        if (forceRefresh || needsTokenRefresh(profile)) {
            val (tokenResponse, exception) = requestToken(profile, isRefresh = true)
            val updatedAuthState = deepCopyAuthState(profile.authState)
            updatedAuthState.update(tokenResponse, exception)
            profileManager.writeProfile(webId, profile.copy(authState = updatedAuthState))
        }
    }

    private fun needsTokenRefresh(profile: Profile): Boolean {
        val expirationTime = profile.authState.lastTokenResponse?.accessTokenExpirationTime ?: return true
        return (System.currentTimeMillis() + 280_000L) > expirationTime
    }

    private fun getUserInfoFromIdToken(idToken: String): UserInfo {
        val webId = getWebIdFromToken(idToken)
        return UserInfo(webId)
    }

    private fun getWebIdFromToken(idToken: String): String {
        return try {
            val payload = idToken.split(".")[1]
            val decoded = android.util.Base64.decode(
                payload,
                android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING,
            )
            val json = org.json.JSONObject(String(decoded))
            json.optString("webid").takeIf { it.isNotEmpty() } ?: json.getString("sub")
        } catch (ex: Exception) {
            throw IllegalStateException("Unable to parse ID token", ex)
        }
    }

    private fun buildInProgressAuthHeaders(httpMethod: String, uri: String): Map<String, String> {
        val profile = inProgressAuth.get() ?: return emptyMap()
        val tokenResponse = profile.authState.lastTokenResponse ?: return emptyMap()
        val headers = mutableMapOf<String, String>()
        headers[HTTPHeaderName.AUTHORIZATION] = "${tokenResponse.tokenType} ${tokenResponse.accessToken}"
        if (tokenResponse.tokenType!!.lowercase() == "dpop") {
            headers[HTTPHeaderName.DPOP] = DPoPGenerator
                .getInstance(profile.authState.authorizationServiceConfiguration!!.discoveryDoc!!)
                .generateProof(httpMethod, uri, tokenResponse.accessToken)
        }
        return headers
    }

    private fun getInProgressTokenResponse(): TokenResponse? {
        return inProgressAuth.get()?.authState?.lastTokenResponse
    }

    private fun updateInProgressDPoPNonce(nonce: String) {
        val profile = inProgressAuth.get() ?: return
        val discoveryDoc = profile.authState.authorizationServiceConfiguration?.discoveryDoc
        if (discoveryDoc != null && discoveryDoc.supportsDPop()) {
            DPoPGenerator.getInstance(discoveryDoc).updateNonce(nonce)
        }
    }

    private fun deepCopyAuthState(authState: AuthState): AuthState {
        return AuthState.jsonDeserialize(authState.jsonSerializeString())
    }
}

fun AuthState.createTokenRequest(isRefresh: Boolean): TokenRequest {
    return if (isRefresh) {
        this.createTokenRefreshRequest()
    } else {
        this.lastAuthorizationResponse!!.createTokenExchangeRequest()
    }
}
