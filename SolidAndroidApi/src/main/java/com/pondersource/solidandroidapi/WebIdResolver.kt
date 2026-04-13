package com.pondersource.solidandroidapi

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidSyncClient
import com.pondersource.shared.HTTPAcceptType
import com.pondersource.shared.HTTPHeaderName
import com.pondersource.shared.RDFSource
import com.pondersource.shared.SolidNetworkResponse
import com.pondersource.shared.data.webid.WebId
import com.pondersource.shared.resource.Resource
import com.pondersource.shared.util.isSuccessful
import com.pondersource.shared.util.toPlainString
import net.openid.appauth.TokenResponse
import java.io.InputStream
import java.net.URI

internal class WebIdResolver {

    suspend fun resolve(
        webIdUri: String,
        tokenProvider: suspend () -> TokenResponse?,
        authHeadersProvider: suspend (httpMethod: String, uri: String) -> Map<String, String>,
        nonceSink: (String) -> Unit,
    ): WebId {
        val response = read(
            URI.create(webIdUri),
            WebId::class.java,
            tokenProvider,
            authHeadersProvider,
            nonceSink,
        )
        if (response is SolidNetworkResponse.Success) {
            return response.data
        } else {
            throw Exception("Could not get the WebID details.")
        }
    }

    private suspend fun <T : Resource> read(
        resource: URI,
        clazz: Class<T>,
        tokenProvider: suspend () -> TokenResponse?,
        authHeadersProvider: suspend (httpMethod: String, uri: String) -> Map<String, String>,
        nonceSink: (String) -> Unit,
    ): SolidNetworkResponse<T> {
        val client: SolidSyncClient = SolidSyncClient.getClient()
        try {
            val tokenResponse = tokenProvider()
                ?: throw IllegalArgumentException("Auth object should be authenticated before interacting with resources.")

            client.session(OpenIdSession.ofIdToken(tokenResponse.idToken!!))

            val response = sendWithDPoPRetry(client, resource, clazz, authHeadersProvider, nonceSink)

            return if (response.isSuccessful()) {
                SolidNetworkResponse.Success(constructObject(response, clazz))
            } else {
                SolidNetworkResponse.Error(response.statusCode(), response.body().toPlainString())
            }
        } catch (e: Exception) {
            return SolidNetworkResponse.Exception(e)
        }
    }

    private suspend fun <T : Resource> sendWithDPoPRetry(
        client: SolidSyncClient,
        resource: URI,
        clazz: Class<T>,
        authHeadersProvider: suspend (httpMethod: String, uri: String) -> Map<String, String>,
        nonceSink: (String) -> Unit,
    ): Response<InputStream> {
        val accept = if (RDFSource::class.java.isAssignableFrom(clazz)) HTTPAcceptType.JSON_LD else HTTPAcceptType.OCTET_STREAM
        val headers = authHeadersProvider("GET", resource.toString())

        val request = Request.newBuilder()
            .uri(resource)
            .header(HTTPHeaderName.ACCEPT, accept)
            .apply { headers.forEach { (key, value) -> header(key, value) } }
            .GET()
            .build()

        val response: Response<InputStream> = client.send(request, Response.BodyHandlers.ofInputStream())

        val dpopNonce = response.headers().firstValue(HTTPHeaderName.DPOP_NONCE).orElse(null)
        if (dpopNonce != null) {
            nonceSink(dpopNonce)
            if (response.statusCode() == 401) {
                val retryHeaders = authHeadersProvider("GET", resource.toString())
                val retryRequest = Request.newBuilder()
                    .uri(resource)
                    .header(HTTPHeaderName.ACCEPT, accept)
                    .apply { retryHeaders.forEach { (key, value) -> header(key, value) } }
                    .GET()
                    .build()
                return client.send(retryRequest, Response.BodyHandlers.ofInputStream())
            }
        }
        return response
    }

    private fun <T> constructObject(
        response: Response<InputStream>,
        clazz: Class<T>,
    ): T {
        val type = response.headers().firstValue(HTTPHeaderName.CONTENT_TYPE)
            .orElse(HTTPAcceptType.OCTET_STREAM)
        val string = response.body().toPlainString()
        if (RDFSource::class.java.isAssignableFrom(clazz)) {
            val options = JsonLdOptions().apply { isRdfStar = true }
            return clazz
                .getConstructor(URI::class.java, MediaType::class.java, RdfDataset::class.java)
                .newInstance(
                    response.uri(),
                    MediaType.of(type),
                    JsonLd.toRdf(JsonDocument.of(string.byteInputStream())).options(options).get(),
                )
        } else {
            return clazz
                .getConstructor(URI::class.java, String::class.java, InputStream::class.java)
                .newInstance(response.uri(), type, string.byteInputStream())
        }
    }
}
