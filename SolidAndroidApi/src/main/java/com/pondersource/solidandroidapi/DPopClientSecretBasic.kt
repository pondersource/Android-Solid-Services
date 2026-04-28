package com.pondersource.solidandroidapi

import android.util.Base64
import com.pondersource.shared.domain.network.HTTPHeaderName
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.internal.UriUtil

class DPopClientSecretBasic(
    private val clientSecret: String,
    private val configuration: AuthorizationServiceConfiguration,
    private val refreshToken: String? = null,
): ClientAuthentication {

    companion object Companion {
        const val NAME: String = "client_secret_basic"
    }

    override fun getRequestHeaders(clientId: String): Map<String?, String?> {
        val encodedClientId = UriUtil.formUrlEncodeValue(clientId)
        val encodedClientSecret = UriUtil.formUrlEncodeValue(clientSecret)
        val credentials = "$encodedClientId:$encodedClientSecret"
        val basicAuth = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        val dpop = DPoPGenerator.getInstance(configuration.discoveryDoc!!)
            .generateProof("POST", configuration.tokenEndpoint.toString(), refreshToken)
        return mapOf(
            HTTPHeaderName.AUTHORIZATION to "Basic $basicAuth",
            HTTPHeaderName.DPOP to dpop
        )
    }

    override fun getRequestParameters(clientId: String): Map<String?, String?> {
        return mapOf("client_id" to clientId)
    }
}