package com.erfangholami.androidsolidservices.api.resource.implementation

import com.erfangholami.androidsolidservices.shared.domain.resource.SolidContainer
import com.erfangholami.androidsolidservices.shared.domain.crud.N3Patch
import com.erfangholami.androidsolidservices.shared.domain.network.HTTPAcceptType
import com.erfangholami.androidsolidservices.shared.domain.network.HTTPHeaderName
import com.erfangholami.androidsolidservices.shared.domain.network.SolidNetworkResponse
import com.erfangholami.androidsolidservices.shared.domain.resource.RDFResource
import com.erfangholami.androidsolidservices.shared.domain.resource.Resource
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidMetadata
import com.erfangholami.androidsolidservices.shared.domain.util.encodeUri
import com.erfangholami.androidsolidservices.shared.vocab.LDP
import com.erfangholami.androidsolidservices.api.auth.Authenticator
import com.erfangholami.androidsolidservices.api.domain.SolidRawResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

internal class SolidHttpClient(private val auth: Authenticator? = null) {

    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    suspend fun send(
        method: String,
        uri: URI,
        contentType: String? = null,
        accept: String? = null,
        linkHeader: String? = null,
        body: ByteArray? = null,
        headers: Map<String, String> = emptyMap(),
    ): SolidRawResponse = withContext(Dispatchers.IO) {
        val mediaType = contentType?.toMediaTypeOrNull()
        val requestBody: RequestBody? = when {
            body != null -> body.toRequestBody(mediaType)
            method in setOf("POST", "PUT", "PATCH") -> ByteArray(0).toRequestBody(null)
            else -> null
        }
        val request = Request.Builder()
            .url(encodeUri(uri).toString())
            .apply {
                if (accept != null) header(HTTPHeaderName.ACCEPT, accept)
                if (contentType != null) header(HTTPHeaderName.CONTENT_TYPE, contentType)
                if (linkHeader != null) header(HTTPHeaderName.LINK, linkHeader)
                headers.forEach { (k, v) -> addHeader(k, v) }
                method(method, requestBody)
            }
            .build()

        val response = httpClient.newCall(request).execute()
        val bodyBytes = response.body?.bytes() ?: ByteArray(0)
        val effectiveUri = try {
            response.request.url.toUri()
        } catch (_: Exception) {
            uri
        }
        val statusCode = response.code
        val responseHeaders = response.headers
        response.close()
        SolidRawResponse(statusCode, responseHeaders, bodyBytes, effectiveUri)
    }

    suspend fun <T : Resource> get(
        webId: String,
        uri: URI,
        clazz: Class<T>
    ): SolidNetworkResponse<T> {
        return try {
            val accept =
                if (RDFResource::class.java.isAssignableFrom(clazz)) HTTPAcceptType.JSON_LD else HTTPAcceptType.ANY
            val response = executeAuthenticated("GET", webId, uri, accept = accept)
            if (response.isSuccessful()) {
                SolidNetworkResponse.Success(SolidResourceParser.parse(response, clazz))
            } else {
                SolidNetworkResponse.Error(response.statusCode, response.body)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    suspend fun <T : Resource> put(
        webId: String,
        resource: T,
        ifMatch: String? = null,
        ifNoneMatchStar: Boolean = false,
    ): SolidNetworkResponse<T> {
        return try {
            val linkType = when {
                SolidContainer::class.java.isAssignableFrom(resource.javaClass) -> "<${LDP.BASIC_CONTAINER}>; rel=\"type\""
                RDFResource::class.java.isAssignableFrom(resource.javaClass) -> "<${LDP.RDF_SOURCE}>; rel=\"type\""
                else -> "<${LDP.NON_RDF_SOURCE}>; rel=\"type\""
            }
            val bodyBytes = resource.getEntity().readBytes()
            val response = executeAuthenticated(
                method = "PUT",
                webId = webId,
                uri = resource.getIdentifier(),
                contentType = resource.getContentType(),
                accept = resource.getContentType(),
                linkHeader = linkType,
                body = bodyBytes,
                ifMatch = ifMatch,
                ifNoneMatchStar = ifNoneMatchStar,
            )
            if (response.isSuccessful()) {
                SolidNetworkResponse.Success(resource)
            } else {
                SolidNetworkResponse.Error(response.statusCode, response.body)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    suspend fun patch(
        webId: String,
        uri: URI,
        patch: N3Patch,
        ifMatch: String? = null
    ): SolidNetworkResponse<Unit> {
        return patchRaw(webId, uri, patch.toN3String(), ifMatch)
    }

    suspend fun patchRaw(
        webId: String,
        uri: URI,
        n3Body: String,
        ifMatch: String? = null
    ): SolidNetworkResponse<Unit> {
        return try {
            val response = executeAuthenticated(
                method = "PATCH",
                webId = webId,
                uri = uri,
                contentType = HTTPAcceptType.N3,
                body = n3Body.toByteArray(Charsets.UTF_8),
                ifMatch = ifMatch,
            )
            if (response.isSuccessful()) {
                SolidNetworkResponse.Success(Unit)
            } else {
                SolidNetworkResponse.Error(response.statusCode, response.body)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    suspend fun head(webId: String, uri: URI): SolidNetworkResponse<SolidMetadata> {
        return try {
            val response = executeAuthenticated("HEAD", webId, uri)
            if (response.isSuccessful()) {
                SolidNetworkResponse.Success(SolidMetadata.from(response.headers))
            } else {
                SolidNetworkResponse.Error(response.statusCode, response.statusCode.toString())
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    suspend fun delete(
        webId: String,
        uri: URI,
        ifMatch: String? = null
    ): SolidNetworkResponse<Boolean> {
        return try {
            val response = executeAuthenticated("DELETE", webId, uri, ifMatch = ifMatch)
            if (response.isSuccessful()) {
                SolidNetworkResponse.Success(true)
            } else {
                SolidNetworkResponse.Error(response.statusCode, response.body)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    suspend fun copy(
        webId: String,
        sourceUri: URI,
        destinationUri: URI,
    ): SolidNetworkResponse<Boolean> {
        return try {
            val response = executeAuthenticated(
                method = "COPY",
                webId = webId,
                uri = sourceUri,
                additionalHeaders = mapOf("Destination" to destinationUri.toString()),
            )
            if (response.isSuccessful()) {
                SolidNetworkResponse.Success(true)
            } else {
                SolidNetworkResponse.Error(response.statusCode, response.body)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    private suspend fun executeAuthenticated(
        method: String,
        webId: String,
        uri: URI,
        contentType: String? = null,
        accept: String? = null,
        linkHeader: String? = null,
        body: ByteArray? = null,
        ifMatch: String? = null,
        ifNoneMatchStar: Boolean = false,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): SolidRawResponse {
        // Up to 3 attempts so a single call can absorb a token-refresh AND a follow-up
        // DPoP-nonce rotation. Force-refresh the access token at most once per call: if a
        // refreshed token still gets a non-nonce 401, surface it.
        var lastResponse: SolidRawResponse? = null
        var didForceRefresh = false

        repeat(MAX_AUTH_ATTEMPTS) {
            val attemptHeaders = buildMap {
                putAll(buildAuthHeaders(webId, method, uri.toString()))
                putAll(additionalHeaders)
                if (ifMatch != null) put(HTTPHeaderName.IF_MATCH, if (ifMatch == "*") "*" else "\"$ifMatch\"")
                if (ifNoneMatchStar) put(HTTPHeaderName.IF_NONE_MATCH, "*")
            }

            val response = send(method, uri, contentType, accept, linkHeader, body, attemptHeaders)
            response.headers[HTTPHeaderName.DPOP_NONCE]?.let { nonce ->
                requireAuth().updateDPoPNonce(webId, nonce)
            }
            lastResponse = response

            if (response.statusCode != 401) return response

            val wwwAuth = response.headers[HTTPHeaderName.WWW_AUTHENTICATE] ?: ""
            val isPureNonceChallenge = wwwAuth.contains("use_dpop_nonce", ignoreCase = true) &&
                    !wwwAuth.contains("invalid_token", ignoreCase = true) &&
                    !wwwAuth.contains("expired_token", ignoreCase = true)

            if (isPureNonceChallenge) {
                return@repeat
            }
            if (didForceRefresh) {
                return response
            }
            requireAuth().getLastTokenResponse(webId, forceRefresh = true)
            didForceRefresh = true
        }
        return lastResponse!!
    }

    private companion object {
        const val MAX_AUTH_ATTEMPTS = 3
    }

    private suspend fun buildAuthHeaders(
        webId: String,
        method: String,
        uri: String
    ): Map<String, String> {
        val authenticator = requireAuth()
        authenticator.getLastTokenResponse(webId)
            ?: throw IllegalArgumentException("Not authenticated. Complete login before accessing Solid resources.")
        return authenticator.getAuthHeaders(webId, method, uri)
    }

    private fun requireAuth(): Authenticator =
        auth ?: throw IllegalStateException(
            "An Authenticator is required for CRUD operations. " +
                    "Construct SolidHttpClient with an Authenticator instance."
        )
}
