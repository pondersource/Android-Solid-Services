package com.pondersource.solidandroidapi

import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.HTTPAcceptType
import com.pondersource.shared.domain.network.HTTPHeaderName
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.RDFResource
import com.pondersource.shared.domain.resource.Resource
import com.pondersource.shared.domain.container.SolidContainer
import com.pondersource.shared.vocab.LDP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

internal class SolidRawResponse(
    val statusCode: Int,
    val headers: Headers,
    val bodyBytes: ByteArray,
    val uri: URI,
) {
    // Decoded lazily; correct for JSON-LD / RDF text; not used for binary content.
    val body: String by lazy { bodyBytes.toString(Charsets.UTF_8) }
    fun isSuccessful(): Boolean = statusCode in 200..299
}

/**
 * Solid-aware HTTP client.
 *
 * ## Two operating modes
 *
 * **Unauthenticated / custom-auth** — pass no [Authenticator] at construction and use [send]
 * with explicit headers. This is the correct mode for the login flow ([WebIdResolver]) where
 * the standard auth state does not yet exist.
 *
 * **Authenticated CRUD** — pass an [Authenticator] at construction, then call [get], [put],
 * or [delete]. DPoP proof generation, nonce refresh, and token management are handled
 * entirely inside this class. Callers never see auth headers.
 */
internal class SolidHttpClient(private val auth: Authenticator? = null) {

    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    // ---- General-purpose send (no automatic auth) ----

    /**
     * Executes a raw HTTP request with no automatic authentication.
     *
     * Pass explicit [headers] when authentication is required but managed externally
     * (e.g. during the login flow before the [Authenticator] is ready).
     */
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
            .url(uri.toString())
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

    // ---- Authenticated CRUD operations ----

    /**
     * Fetches and parses a Solid resource into [clazz].
     *
     * Automatically selects `Accept: application/ld+json` for RDF resources.
     * DPoP proof and token headers are injected and refreshed as needed.
     *
     * Requires an [Authenticator] at construction.
     */
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

    /**
     * Writes a Solid resource via HTTP PUT.
     *
     * Sets the correct `Link: rel="type"` header (LDP BasicContainer / RDFSource / NonRDFSource)
     * derived from [resource]'s actual type. Auth headers are injected automatically.
     *
     * Pass [ifMatch] (an ETag value without quotes) to issue a conditional PUT that fails with
     * 412 if the server's current ETag differs — preventing lost-update races.
     *
     * Requires an [Authenticator] at construction.
     */
    suspend fun <T : Resource> put(
        webId: String,
        resource: T,
        ifMatch: String? = null
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

    /**
     * Applies an N3 Patch to a Solid RDF resource via HTTP PATCH.
     *
     * The patch body is serialized as `text/n3` with `solid:InsertDeletePatch`.
     * Use [N3Patch.build] or [N3Patch.fromDiff] to construct the patch without
     * writing raw N3 strings.
     *
     * Requires an [Authenticator] at construction.
     */
    suspend fun patch(webId: String, uri: URI, patch: N3Patch): SolidNetworkResponse<Unit> {
        return try {
            val response = executeAuthenticated(
                method = "PATCH",
                webId = webId,
                uri = uri,
                contentType = patch.contentType,
                body = patch.toInputStream().readBytes(),
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

    /**
     * Applies a pre-serialised N3 Patch body to a Solid RDF resource via HTTP PATCH.
     *
     * Identical to [patch] but accepts a raw `text/n3` string instead of a typed [N3Patch].
     * Used when the patch document has been transported as a string (e.g. over AIDL).
     *
     * Requires an [Authenticator] at construction.
     */
    suspend fun patchRaw(webId: String, uri: URI, n3Body: String): SolidNetworkResponse<Unit> {
        return try {
            val response = executeAuthenticated(
                method = "PATCH",
                webId = webId,
                uri = uri,
                contentType = com.pondersource.shared.domain.network.HTTPAcceptType.N3,
                body = n3Body.toByteArray(Charsets.UTF_8),
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

    /**
     * Deletes the Solid resource at [uri].
     *
     * Auth headers are injected automatically.
     * Requires an [Authenticator] at construction.
     */
    suspend fun delete(webId: String, uri: URI): SolidNetworkResponse<Boolean> {
        return try {
            val response = executeAuthenticated("DELETE", webId, uri)
            if (response.isSuccessful()) {
                SolidNetworkResponse.Success(true)
            } else {
                SolidNetworkResponse.Error(response.statusCode, response.body)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    // ---- Internal auth machinery ----

    /**
     * Executes an authenticated HTTP request with DPoP-nonce retry.
     *
     * Per Solid-OIDC: if the server responds with a `DPoP-Nonce` header, the nonce is
     * stored via [Authenticator.updateDPoPNonce] and the request is retried once on 401.
     * On the retry the DPoP proof includes the new nonce, satisfying the challenge.
     *
     * [ifMatch] — when non-null, adds `If-Match: "<value>"` for a conditional request.
     */
    private suspend fun executeAuthenticated(
        method: String,
        webId: String,
        uri: URI,
        contentType: String? = null,
        accept: String? = null,
        linkHeader: String? = null,
        body: ByteArray? = null,
        ifMatch: String? = null,
    ): SolidRawResponse {
        val authHeaders = buildAuthHeaders(webId, method, uri.toString())
        val extraHeaders = if (ifMatch != null)
            authHeaders + mapOf(HTTPHeaderName.IF_MATCH to "\"$ifMatch\"")
        else
            authHeaders

        var response = send(method, uri, contentType, accept, linkHeader, body, extraHeaders)

        // Always update DPoP nonce when the server provides one.
        response.headers[HTTPHeaderName.DPOP_NONCE]?.let { nonce ->
            requireAuth().updateDPoPNonce(webId, nonce)
        }

        // Retry on 401: could be an expired token, a fresh DPoP-nonce challenge, or both.
        // Force-refresh the access token so the retry carries fresh credentials.
        if (response.statusCode == 401) {
            requireAuth().getLastTokenResponse(webId, forceRefresh = true)
            val retryHeaders = buildAuthHeaders(webId, method, uri.toString()).let { headers ->
                if (ifMatch != null) headers + mapOf(HTTPHeaderName.IF_MATCH to "\"$ifMatch\"") else headers
            }
            response = send(method, uri, contentType, accept, linkHeader, body, retryHeaders)
            // Pick up any nonce the server sends with the retry response as well.
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
