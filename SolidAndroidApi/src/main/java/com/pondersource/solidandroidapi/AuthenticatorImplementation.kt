package com.pondersource.solidandroidapi

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.openid.OpenIdConfig
import com.inrupt.client.openid.OpenIdException
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidSyncClient
import com.pondersource.shared.HTTPAcceptType
import com.pondersource.shared.HTTPHeaderName
import com.pondersource.shared.RDFSource
import com.pondersource.shared.SolidNetworkResponse
import com.pondersource.shared.data.Profile
import com.pondersource.shared.data.ProfileList
import com.pondersource.shared.data.UserInfo
import com.pondersource.shared.data.getProfileOrReturnDefault
import com.pondersource.shared.data.webid.WebId
import com.pondersource.shared.resource.Resource
import com.pondersource.shared.util.isSuccessful
import com.pondersource.shared.util.toPlainString
import com.pondersource.solidandroidapi.repository.UserRepository
import com.pondersource.solidandroidapi.repository.UserRepositoryImplementation
import io.jsonwebtoken.Jwts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver
import java.io.InputStream
import java.net.URI
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authenticator is responsible to do the Authentication phase with the selected Identity Provider.
 */
class AuthenticatorImplementation : Authenticator {

    companion object {

        private const val IN_PROGRESS_AUTH = "in_progress"

        @Volatile
        private var INSTANCE: Authenticator? = null

        fun getInstance(
            context: Context,
        ): Authenticator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthenticatorImplementation(context).also { INSTANCE = it }
            }
        }
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initDeferred = CompletableDeferred<Unit>()

    private val userRepository: UserRepository
    private val authService: AuthorizationService
    private var profiles: ProfileList = ProfileList()
    private var activeWebId: String? = null
    private val _activeProfileFlow = MutableStateFlow(Profile())
    override val activeProfileFlow: StateFlow<Profile> get() = _activeProfileFlow

    private constructor(
        context: Context,
    ) {
        this.userRepository = UserRepositoryImplementation.getInstance(context)
        this.authService = AuthorizationService(context)
        // Load persisted state on a background thread — never blocks the calling thread.
        applicationScope.launch {
            profiles = userRepository.readAllProfilesOnce()
            activeWebId = userRepository.getActiveWebId()
            if (getAllLoggedInProfiles().isNotEmpty()) {
                _activeProfileFlow.value = getProfile()
            }
            initDeferred.complete(Unit)
        }
    }

    /** Suspends until the initial DataStore load has completed. */
    private suspend fun awaitInit() = initDeferred.await()

    private suspend fun refreshProfiles() {
        profiles = userRepository.readAllProfilesOnce()
    }

    private suspend fun updateRegistrationResponse(
        regResponse: net.openid.appauth.RegistrationResponse?
    ) {
        val profile = profiles.getProfileOrReturnDefault(IN_PROGRESS_AUTH).apply {
            authState.update(regResponse)
        }
        userRepository.writeProfile(IN_PROGRESS_AUTH, profile)
        refreshProfiles()
    }

    private suspend fun updateAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        val profile = profiles.getProfileOrReturnDefault(IN_PROGRESS_AUTH).apply {
            authState.update(authResponse, authException)
        }
        userRepository.writeProfile(IN_PROGRESS_AUTH, profile)
        refreshProfiles()
    }

    private suspend fun updateTokenResponse(
        webid: String,
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ) {
        val profile = profiles.getProfileOrReturnDefault(webid).apply {
            authState.update(tokenResponse, authException)
        }
        userRepository.writeProfile(webid, profile)
        refreshProfiles()
    }

    private fun getUserInfoFromIdToken(webId: String): UserInfo? {
        if (isUserAuthorized()) {
            val profile = getProfile(webId)
            val actualWebId = getWebId(profile.authState.idToken!!)
            return UserInfo(actualWebId)
        } else {
            return null
            //TODO(Not authorized user)
        }
    }

    private suspend fun getAuthorizationConf(
        oidcIssuer: String
    ): Pair<AuthorizationServiceConfiguration?, AuthorizationException?>{
        return suspendCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(oidcIssuer.toUri()) { serviceConfiguration, exception ->
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

    private suspend fun requestToken(
        webid: String,
        tokenRefreshRequest: Boolean = false
    ): Pair<TokenResponse?, AuthorizationException?> {

        val profile = profiles.getProfileOrReturnDefault(webid)
        val result : Pair<TokenResponse?, AuthorizationException?> = if (profile.authState.lastAuthorizationResponse != null) {
            val tokenRequest = profile.authState.createTokenRequest(tokenRefreshRequest)
            val clientAuthentication = if(profile.authState.authorizationServiceConfiguration!!.discoveryDoc!!.supportsDPop()) {
                DPopClientSecretBasic(
                    clientSecret = profile.authState.lastRegistrationResponse!!.clientSecret!!,
                    configuration = tokenRequest.configuration,
                    refreshToken = profile.authState.refreshToken
                )
            } else {
                ClientSecretBasic(
                    profile.authState.lastRegistrationResponse!!.clientSecret!!,
                )
            }
            suspendCoroutine { cont ->
                authService.performTokenRequest(
                    tokenRequest,
                    clientAuthentication
                ) { tokenResponse, exception ->
                    // Run the DataStore write on the IO dispatcher, then resume the
                    // suspended caller — avoids blocking the AppAuth callback thread.
                    applicationScope.launch {
                        updateTokenResponse(webid, tokenResponse, exception)
                        cont.resume(Pair(tokenResponse, exception))
                    }
                }
            }
        } else {
            Pair(null, profile.authState.authorizationException)
        }
        return result
    }

    private suspend fun getWebIdProfile(webId: String): WebId {
        val webIdResponse = read(URI.create(webId), WebId::class.java)
        if (webIdResponse is SolidNetworkResponse.Success) {
            return webIdResponse.data
        } else {
            throw Exception("Could not get the webId details.")
        }
    }

    private suspend fun <T: Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T> {

        val client: SolidSyncClient = SolidSyncClient.getClient()
        try {

            val tokenResponse = getLastTokenResponse(IN_PROGRESS_AUTH)
                ?: throw IllegalArgumentException("Auth object should be authenticated before interacting with resources.")

            client.session(OpenIdSession.ofIdToken(tokenResponse.idToken!!))

            val response = sendWithDPoPRetry(client, IN_PROGRESS_AUTH, "GET", resource, clazz)

            return if (response.isSuccessful()) {
                SolidNetworkResponse.Success(constructObject(response, clazz))
            } else {
                SolidNetworkResponse.Error(response.statusCode(), response.body().toPlainString())
            }
        } catch (e: Exception) {
            return SolidNetworkResponse.Exception(e)
        }
    }

    private suspend fun <T: Resource> sendWithDPoPRetry(
        client: SolidSyncClient,
        webId: String,
        httpMethod: String,
        resource: URI,
        clazz: Class<T>,
    ): Response<InputStream> {
        val headers = getAuthHeaders(webId, httpMethod, resource.toString())

        val request = Request.newBuilder()
            .uri(resource)
            .header(HTTPHeaderName.ACCEPT, if (RDFSource::class.java.isAssignableFrom(clazz)) HTTPAcceptType.JSON_LD else HTTPAcceptType.OCTET_STREAM)
            .apply {
                headers.forEach { (key, value) ->
                    this.header(key, value)
                }
            }
            .GET()
            .build()

        val response: Response<InputStream> = client.send(
            request,
            Response.BodyHandlers.ofInputStream()
        )

        val dpopNonce = response.headers().firstValue(HTTPHeaderName.DPOP_NONCE).orElse(null)
        if (dpopNonce != null) {
            updateDPoPNonce(webId, dpopNonce)
            if (response.statusCode() == 401) {
                val retryHeaders = getAuthHeaders(webId, httpMethod, resource.toString())
                val retryRequest = Request.newBuilder()
                    .uri(resource)
                    .header(HTTPHeaderName.ACCEPT, if (RDFSource::class.java.isAssignableFrom(clazz)) HTTPAcceptType.JSON_LD else HTTPAcceptType.OCTET_STREAM)
                    .apply {
                        retryHeaders.forEach { (key, value) ->
                            this.header(key, value)
                        }
                    }
                    .GET()
                    .build()

                return client.send(retryRequest, Response.BodyHandlers.ofInputStream())
            }
        }
        return response
    }

    private fun <T> constructObject(
        response: Response<InputStream>,
        clazz: Class<T>
    ): T {
        val type = response.headers().firstValue(HTTPHeaderName.CONTENT_TYPE)
            .orElse(HTTPAcceptType.OCTET_STREAM)
        val string = response.body().toPlainString()
        if (RDFSource::class.java.isAssignableFrom(clazz)) {
            val options = JsonLdOptions().apply {
                isRdfStar = true
            }
            return clazz
                .getConstructor(URI::class.java, MediaType::class.java, RdfDataset::class.java)
                .newInstance(response.uri(), MediaType.of(type), JsonLd.toRdf(
                    JsonDocument.of(string.byteInputStream())).options(options).get())
        } else {
            return clazz
                .getConstructor(URI::class.java, String::class.java, InputStream::class.java)
                .newInstance(response.uri(), type, string.byteInputStream())
        }
    }


    override suspend fun createAuthenticationIntentWithWebId(
        webId: String,
        redirectUri: String,
    ) : Pair<Intent?, String?> {
        awaitInit()
        val webIdDetails = getWebIdProfile(webId)

        return createAuthenticationIntentWithOidcIssuer(
            webIdDetails.getOidcIssuers()[0].toString(),
            redirectUri
        )
    }

    override suspend fun createAuthenticationIntentWithOidcIssuer(
        oidcIssuer: String,
        redirectUri: String,
    ) : Pair<Intent?, String?> {
        awaitInit()
        val conf = getAuthorizationConf(oidcIssuer)

        return if (conf.first != null) {

            registerToOpenId(conf.first!!, redirectUri)
            val profile = profiles.getProfileOrReturnDefault(IN_PROGRESS_AUTH)

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

    override suspend fun submitAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ): String? {
        awaitInit()
        updateAuthorizationResponse(authResponse, authException)
        if (authException == null && authResponse != null) {
            val tokenResponse = requestToken(IN_PROGRESS_AUTH)
            if(tokenResponse.second == null && tokenResponse.first != null) {
                val userInfo = getUserInfoFromIdToken(IN_PROGRESS_AUTH)
                val webId = getWebIdProfile(userInfo!!.webId)

                val loggedInProfile = profiles.getProfileOrReturnDefault(IN_PROGRESS_AUTH).apply {
                    this.userInfo = userInfo
                    this.webId = webId
                }

                val newWebId = loggedInProfile.userInfo!!.webId
                userRepository.writeProfile(newWebId, loggedInProfile)
                userRepository.removeProfile(IN_PROGRESS_AUTH)
                // Set the newly logged-in account as active
                userRepository.setActiveWebId(newWebId)
                activeWebId = newWebId
                refreshProfiles()
                _activeProfileFlow.value = getProfile()
                return newWebId
            } else {
                return ""
            }
        } else {
            return null
        }
    }

    override suspend fun getLastTokenResponse(
        webId: String,
        forceRefresh: Boolean
    ): TokenResponse? {
        awaitInit()
        checkTokenAndRefresh(webId, forceRefresh)
        val profile = profiles.getProfileOrReturnDefault(webId)
        return profile.authState.lastTokenResponse
    }

    override suspend fun getAuthHeaders(
        webId: String,
        httpMethod: String,
        uri: String
    ): Map<String, String> {
        awaitInit()
        val headers = mutableMapOf<String, String>()

        val profile = profiles.getProfileOrReturnDefault(webId)
        val tokenResponse = getLastTokenResponse(webId)

        headers[HTTPHeaderName.AUTHORIZATION] = "${tokenResponse!!.tokenType} ${tokenResponse.accessToken}"
        if(tokenResponse.tokenType!!.lowercase() == "dpop") {
            headers[HTTPHeaderName.DPOP] = DPoPGenerator.getInstance(profile.authState.authorizationServiceConfiguration!!.discoveryDoc!!)
                .generateProof(httpMethod, uri, tokenResponse.accessToken)
        }
        return headers
    }

    override fun updateDPoPNonce(
        webId: String,
        nonce: String,
    ) {
        val profile = profiles.getProfileOrReturnDefault(webId)
        val discoveryDoc = profile.authState.authorizationServiceConfiguration?.discoveryDoc
        if (discoveryDoc != null && discoveryDoc.supportsDPop()) {
            DPoPGenerator.getInstance(discoveryDoc).updateNonce(nonce)
        }
    }

    private suspend fun checkTokenAndRefresh(
        webid: String,
        forceRefresh: Boolean = false
    ) {
        val profile = profiles.getProfileOrReturnDefault(webid)
        if (forceRefresh || needsTokenRefresh(profile)){
            requestToken(webid, true)
        } else {
            //Token is still valid
        }
    }

    private fun needsTokenRefresh(profile: Profile): Boolean {
        return (System.currentTimeMillis() + 280_000L) > profile.authState.lastTokenResponse!!.accessTokenExpirationTime!!
    }

    override fun isUserAuthorized(): Boolean {
        return profiles.profiles.values.firstOrNull { it.authState.isAuthorized } != null
    }

    override fun getAllLoggedInProfiles(): List<Profile> {
        return profiles.profiles.values.filter { it.authState.isAuthorized }.filter { it.userInfo != null && it.webId != null }
    }

    override fun getProfile(
        webId: String,
    ) = profiles.getProfileOrReturnDefault(webId)

    override fun getProfile(): Profile {
        val allProfiles = getAllLoggedInProfiles()
        // Return the active profile if set and still valid
        if (activeWebId != null) {
            val activeProfile = allProfiles.find { it.userInfo?.webId == activeWebId }
            if (activeProfile != null) return activeProfile
        }
        // Fallback to first authorized profile, or an empty profile if not yet initialized
        return allProfiles.firstOrNull() ?: Profile()
    }

    override suspend fun getActiveWebId(): String? {
        awaitInit()
        return activeWebId
    }

    override suspend fun setActiveWebId(webId: String) {
        awaitInit()
        val profile = profiles.getProfileOrReturnDefault(webId)
        if (profile.authState.isAuthorized && profile.userInfo != null) {
            activeWebId = webId
            userRepository.setActiveWebId(webId)
            _activeProfileFlow.value = getProfile()
        } else {
            throw IllegalArgumentException("Cannot set active account: profile for $webId is not authorized or incomplete.")
        }
    }

    override suspend fun resetProfile() {
        awaitInit()
        userRepository.removeAllProfiles()
        userRepository.setActiveWebId(null)
        activeWebId = null
        refreshProfiles()
    }

    override suspend fun resetProfile(
        webId: String,
    ) {
        awaitInit()
        userRepository.removeProfile(webId)
        // If the removed profile was the active one, switch to another or clear
        if (activeWebId == webId) {
            refreshProfiles()
            val remaining = getAllLoggedInProfiles()
            if (remaining.isNotEmpty()) {
                val newActiveWebId = remaining.first().userInfo!!.webId
                activeWebId = newActiveWebId
                userRepository.setActiveWebId(newActiveWebId)
                _activeProfileFlow.value = getProfile()
            } else {
                activeWebId = null
                userRepository.setActiveWebId(null)
            }
        } else {
            refreshProfiles()
        }
    }

    override suspend fun getTerminationSessionIntent(
        webId: String,
        logoutRedirectUrl: String,
    ) : Pair<Intent?, String?>{
        awaitInit()
        val profile = profiles.getProfileOrReturnDefault(webId)
        return if (profile.authState.lastAuthorizationResponse != null &&
            profile.authState.authorizationServiceConfiguration != null) {

            val token =  getLastTokenResponse(webId)
            val endSessionReq = EndSessionRequest.Builder(profile.authState.authorizationServiceConfiguration!!)
                .setIdTokenHint(token!!.idToken)
                .setPostLogoutRedirectUri(Uri.parse(logoutRedirectUrl))
                .build()
            Pair(authService.getEndSessionRequestIntent(endSessionReq), null)
        } else {
            Pair(null, "There is no configuration")
        }
    }

    private fun parseIdToken(idToken: String, config: OpenIdConfig): JwtClaims {
        return try {
            val builder = JwtConsumerBuilder()

            // Required by OpenID Connect
            builder.setRequireExpirationTime()
            builder.setExpectedIssuers(true, *arrayOfNulls<String>(0))
            builder.setRequireSubject()
            builder.setRequireIssuedAt()

            // If a grace period is set, allow for some clock skew
            if (config.expGracePeriodSecs > 0) {
                builder.setAllowedClockSkewInSeconds(config.expGracePeriodSecs)
            } else {
                builder.setEvaluationTime(NumericDate.fromSeconds(Instant.now().epochSecond))
            }

            // If an expected audience is set, verify that we have the correct value
            if (config.expectedAudience != null) {
                builder.setExpectedAudience(true, config.expectedAudience)
            } else {
                builder.setSkipDefaultAudienceValidation()
            }

            // If a JWKS location is set, perform signature validation
            if (config.publicKeyLocation != null) {
                val jwks = HttpsJwks(config.publicKeyLocation.toString())
                val resolver = HttpsJwksVerificationKeyResolver(jwks)
                builder.setVerificationKeyResolver(resolver)
            } else {
                builder.setSkipSignatureVerification()
            }

            val consumer = builder.build()
            consumer.processToClaims(idToken)
        } catch (ex: InvalidJwtException) {
            throw OpenIdException("Unable to parse ID token", ex)
        }
    }

    private fun getWebId(idToken: String): String {
        Jwts.builder()
        val claims = parseIdToken(idToken, OpenIdConfig())
        val webId = claims.getClaimValueAsString("webid")
        return webId ?: claims.getClaimValueAsString("sub")
    }
}

fun AuthState.createTokenRequest(isRefresh: Boolean): TokenRequest {
    return if(isRefresh) {
        this.createTokenRefreshRequest()
    } else {
        this.lastAuthorizationResponse!!.createTokenExchangeRequest()
    }
}
