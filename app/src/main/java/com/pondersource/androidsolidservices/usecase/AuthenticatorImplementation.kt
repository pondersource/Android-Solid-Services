package com.pondersource.androidsolidservices.usecase

import android.content.Intent
import android.net.Uri
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidSyncClient
import com.pondersource.androidsolidservices.repository.UserRepository
import com.pondersource.solidandroidclient.HTTPAcceptType.JSON_LD
import com.pondersource.solidandroidclient.HTTPAcceptType.OCTET_STREAM
import com.pondersource.solidandroidclient.HTTPHeaderName.ACCEPT
import com.pondersource.solidandroidclient.HTTPHeaderName.AUTHORIZATION
import com.pondersource.solidandroidclient.HTTPHeaderName.CONTENT_TYPE
import com.pondersource.solidandroidclient.SolidNetworkResponse
import com.pondersource.solidandroidclient.data.Profile
import com.pondersource.solidandroidclient.data.UserInfo
import com.pondersource.solidandroidclient.data.WebIdProfile
import com.pondersource.solidandroidclient.RDFSource
import com.pondersource.solidandroidclient.sub.resource.Resource
import com.pondersource.solidandroidclient.util.Utils
import com.pondersource.solidandroidclient.util.isSuccessful
import com.pondersource.solidandroidclient.util.toPlainString
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
import java.io.InputStream
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authenticator is responsible to do the Authentication phase with the selected Identity Provider.
 */
class AuthenticatorImplementation (
    private var userRepository: UserRepository,
    private val authService: AuthorizationService,
) : Authenticator {

    private var profile: Profile

    init {
        this.profile = userRepository.readProfile()
    }

    private fun updateRegistrationResponse(
        regResponse: RegistrationResponse?
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

    private suspend fun getUserInfo(): UserInfo? {
        if (isUserAuthorized()) {
            checkTokenAndRefresh()
            val webId = Utils.getWebId(profile.authState.idToken)
            return UserInfo(webId)
        } else {
            return null
            //TODO(Not authorized user)
        }
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

    /**
     * Refreshes the token in case it has been expired.
     * @return RefreshTokenResponse which contains accessToken, tokenId and exception
     */
    private suspend fun refreshToken(): Pair<TokenResponse?, AuthorizationException?> {
        val result : Pair<TokenResponse?, AuthorizationException?> = if (profile.authState.lastAuthorizationResponse != null) {
            suspendCoroutine { cont ->
                authService.performTokenRequest(
                    profile.authState.createTokenRefreshRequest(),
                    ClientSecretBasic(profile.authState.lastRegistrationResponse!!.clientSecret!!)
                ) { tokenResponse, exception ->
                    cont.resume(Pair(tokenResponse, exception))
                }
            }
        } else {
            Pair(null, profile.authState.authorizationException)
        }
        updateTokenResponse(result.first, result.second)
        return Pair(result.first, result.second)
    }

    private suspend fun getWebIdProfile(webId: String): WebIdProfile {
        val webIdProfileResponse = read(URI.create(webId), WebIdProfile::class.java)
        if (webIdProfileResponse is SolidNetworkResponse.Success) {
            return webIdProfileResponse.data
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
            val tokenResponse = if (needsTokenRefresh()) {
                val tokenResponse = getLastTokenResponse()
                client.session(OpenIdSession.ofIdToken(tokenResponse!!.idToken!!))
                tokenResponse
            } else {
                getLastTokenResponse()
            }

            val request = Request.newBuilder()
                .uri(resource)
                .header(ACCEPT, if (RDFSource::class.java.isAssignableFrom(clazz)) JSON_LD else OCTET_STREAM)
                .header(AUTHORIZATION, "${tokenResponse?.tokenType} ${tokenResponse?.accessToken}")
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
        val type = response.headers().firstValue(CONTENT_TYPE)
            .orElse(OCTET_STREAM)
        val string = response.body().toPlainString()
        if (RDFSource::class.java.isAssignableFrom(clazz)) {
            val options = JsonLdOptions().apply {
                isRdfStar = true
            }
            return clazz
                .getConstructor(URI::class.java, MediaType::class.java, RdfDataset::class.java)
                .newInstance(response.uri(), MediaType.of(type), JsonLd.toRdf(JsonDocument.of(string.byteInputStream())).options(options).get())
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
            requestToken()
            profile.userInfo = getUserInfo()
            profile.webId = getWebIdProfile(profile.userInfo!!.webId)
            userRepository.writeProfile(profile)
        }
    }

    override suspend fun getLastTokenResponse(): TokenResponse? {
        checkTokenAndRefresh()
        return profile.authState.lastTokenResponse
    }

    private suspend fun checkTokenAndRefresh() {
        if (needsTokenRefresh()){
            refreshToken()
        } else {
            //Token is still valid
        }
    }

    override fun needsTokenRefresh(): Boolean {
        return (System.currentTimeMillis() + 10_000L) > profile.authState.lastTokenResponse!!.accessTokenExpirationTime!!
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
}
