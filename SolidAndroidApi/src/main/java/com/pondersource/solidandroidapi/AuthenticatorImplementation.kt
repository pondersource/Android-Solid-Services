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
import com.pondersource.shared.data.UserInfo
import com.pondersource.shared.data.webid.WebId
import com.pondersource.shared.resource.Resource
import com.pondersource.shared.util.isSuccessful
import com.pondersource.shared.util.toPlainString
import com.pondersource.solidandroidapi.repository.UserRepository
import com.pondersource.solidandroidapi.repository.UserRepositoryImplementation
import io.jsonwebtoken.Jwts
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
        @Volatile
        private lateinit var INSTANCE: Authenticator

        fun getInstance(
            context: Context,
        ): Authenticator {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = AuthenticatorImplementation(context)
                INSTANCE
            }
        }
    }

    private val userRepository: UserRepository
    private val authService: AuthorizationService
    private var profile: Profile

    private constructor(
        context: Context,
    ) {
        this.userRepository = UserRepositoryImplementation.getInstance(context)
        this.authService = AuthorizationService(context)
        this.profile = userRepository.readProfile()
    }

    private fun updateRegistrationResponse(
        regResponse: net.openid.appauth.RegistrationResponse?
    ) {
        profile.authState.update(regResponse)
        userRepository.writeProfile(profile)
    }

    private fun updateAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        profile.authState.update(authResponse, authException)
        userRepository.writeProfile(profile)
    }

    private fun updateTokenResponse(
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ) {
        profile.authState.update(tokenResponse, authException)
        userRepository.writeProfile(profile)
    }

    private fun getUserInfoFromIdToken(): UserInfo? {
        if (isUserAuthorized()) {
            val webId = getWebId(profile.authState.idToken!!)
            return UserInfo(webId)
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

    private suspend fun requestToken(tokenRefreshRequest: Boolean = false): Pair<TokenResponse?, AuthorizationException?> {

        val result : Pair<TokenResponse?, AuthorizationException?> = if (profile.authState.lastAuthorizationResponse != null) {
            suspendCoroutine { cont ->
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
                authService.performTokenRequest(
                    tokenRequest,
                    clientAuthentication
                ) { tokenResponse, exception ->
                    updateTokenResponse(tokenResponse, exception)
                    cont.resume(Pair(tokenResponse, exception))
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

    suspend fun <T: Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T> {

        val client: SolidSyncClient = SolidSyncClient.getClient()
        try {

            val tokenResponse = getLastTokenResponse()
                ?: throw IllegalArgumentException("Auth object should be authenticated before interacting with resources.")

            client.session(OpenIdSession.ofIdToken(tokenResponse.idToken!!))
            val headers = getAuthHeaders("GET", resource.toString())

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

            return if (response.isSuccessful()) {
                SolidNetworkResponse.Success(constructObject(response, clazz))
            } else {
                SolidNetworkResponse.Error(response.statusCode(), response.body().toPlainString())
            }
        } catch (e: Exception) {
            return SolidNetworkResponse.Exception(e)
        }
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

    override suspend fun submitAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        updateAuthorizationResponse(authResponse, authException)
        if (authException == null && authResponse != null) {
            val tokenResponse = requestToken()
            if(tokenResponse.second == null && tokenResponse.first != null) {
                profile.userInfo = getUserInfoFromIdToken()
                profile.webId = getWebIdProfile(profile.userInfo!!.webId)
                userRepository.writeProfile(profile)
            }
        }
    }

    override suspend fun getLastTokenResponse(
        forceRefresh: Boolean
    ): TokenResponse? {
        checkTokenAndRefresh(forceRefresh)
        return profile.authState.lastTokenResponse
    }

    override suspend fun getAuthHeaders(httpMethod: String, uri: String): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        val tokenResponse = getLastTokenResponse()

        headers[HTTPHeaderName.AUTHORIZATION] = "${tokenResponse!!.tokenType} ${tokenResponse.accessToken}"
        if(tokenResponse.tokenType!!.lowercase() == "dpop") {
            headers[HTTPHeaderName.DPOP] = DPoPGenerator.getInstance(profile.authState.authorizationServiceConfiguration!!.discoveryDoc!!)
                .generateProof(httpMethod, uri, tokenResponse.accessToken)
        }
        return headers
    }

    private suspend fun checkTokenAndRefresh(forceRefresh: Boolean = false) {
        if (forceRefresh || needsTokenRefresh()){
            requestToken(true)
        } else {
            //Token is still valid
        }
    }

    private fun needsTokenRefresh(): Boolean {
        return (System.currentTimeMillis() + 280_000L) > profile.authState.lastTokenResponse!!.accessTokenExpirationTime!!
    }

    override fun isUserAuthorized(): Boolean {
        return profile.authState.isAuthorized
    }

    override fun getProfile() = profile

    override fun resetProfile() {
        profile = Profile()
        userRepository.writeProfile(profile)
    }

    override suspend fun getTerminationSessionIntent(
        logoutRedirectUrl: String,
    ) : Pair<Intent?, String?>{

        return if (profile.authState.lastAuthorizationResponse != null &&
            profile.authState.authorizationServiceConfiguration != null) {

            val token =  getLastTokenResponse()
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
