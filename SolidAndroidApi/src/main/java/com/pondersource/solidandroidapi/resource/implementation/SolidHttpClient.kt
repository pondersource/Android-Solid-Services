package com.pondersource.solidandroidapi.resource.implementation

import com.pondersource.shared.domain.resource.SolidContainer
import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.HTTPAcceptType
import com.pondersource.shared.domain.network.HTTPHeaderName
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.RDFResource
import com.pondersource.shared.domain.resource.Resource
import com.pondersource.shared.domain.resource.SolidMetadata
import com.pondersource.shared.domain.util.encodeUri
import com.pondersource.shared.vocab.LDP
import com.pondersource.solidandroidapi.auth.Authenticator
import com.pondersource.solidandroidapi.domain.SolidRawResponse
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
    ): SolidRawResponse {
        val authHeaders = buildAuthHeaders(webId, method, uri.toString())
        val extraHeaders = buildMap {
            putAll(authHeaders)
            if (ifMatch != null) put(HTTPHeaderName.IF_MATCH, if (ifMatch == "*") "*" else "\"$ifMatch\"")
            if (ifNoneMatchStar) put(HTTPHeaderName.IF_NONE_MATCH, "*")
        }

        var response = send(method, uri, contentType, accept, linkHeader, body, extraHeaders)

        response.headers[HTTPHeaderName.DPOP_NONCE]?.let { nonce ->
            requireAuth().updateDPoPNonce(webId, nonce)
        }

        if (response.statusCode == 401) {
            val wwwAuth = response.headers[HTTPHeaderName.WWW_AUTHENTICATE] ?: ""
            val isPureNonceChallenge = wwwAuth.contains("use_dpop_nonce", ignoreCase = true) &&
                    !wwwAuth.contains("invalid_token", ignoreCase = true) &&
                    !wwwAuth.contains("expired_token", ignoreCase = true)

            if (!isPureNonceChallenge) {
                requireAuth().getLastTokenResponse(webId, forceRefresh = true)
            }

            val retryHeaders = buildMap {
                putAll(buildAuthHeaders(webId, method, uri.toString()))
                if (ifMatch != null) put(HTTPHeaderName.IF_MATCH, if (ifMatch == "*") "*" else "\"$ifMatch\"")
                if (ifNoneMatchStar) put(HTTPHeaderName.IF_NONE_MATCH, "*")
            }
            response = send(method, uri, contentType, accept, linkHeader, body, retryHeaders)
            response.headers[HTTPHeaderName.DPOP_NONCE]?.let { nonce ->
                requireAuth().updateDPoPNonce(webId, nonce)
            }
        }

        return response
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
