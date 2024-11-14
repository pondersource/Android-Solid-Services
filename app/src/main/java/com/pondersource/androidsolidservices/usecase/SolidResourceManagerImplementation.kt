package com.pondersource.androidsolidservices.usecase

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.jsonld.json.JsonProvider
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidSyncClient
import com.pondersource.solidandroidclient.HTTPAcceptType.ANY
import com.pondersource.solidandroidclient.HTTPAcceptType.JSON_LD
import com.pondersource.solidandroidclient.HTTPAcceptType.OCTET_STREAM
import com.pondersource.solidandroidclient.HTTPHeaderName.ACCEPT
import com.pondersource.solidandroidclient.HTTPHeaderName.AUTHORIZATION
import com.pondersource.solidandroidclient.HTTPHeaderName.CONTENT_TYPE
import com.pondersource.solidandroidclient.SolidNetworkResponse
import com.pondersource.solidandroidclient.RDFSource
import com.pondersource.solidandroidclient.sub.resource.Resource
import com.pondersource.solidandroidclient.util.isSuccessful
import com.pondersource.solidandroidclient.util.toPlainString
import jakarta.json.JsonArray
import jakarta.json.JsonString
import net.openid.appauth.TokenResponse
import java.io.InputStream
import java.net.URI


class SolidResourceManagerImplementation(
    private val auth: Authenticator
) : SolidResourceManager {

    private val client: SolidSyncClient = SolidSyncClient.getClient()

    private fun updateClientWithNewToken(newTokenId: String) {
        client.session(OpenIdSession.ofIdToken(newTokenId))
    }

    private suspend fun handleTokenRefreshAndReturn(): TokenResponse? {
        return if (auth.needsTokenRefresh()) {
            val tokenResponse = auth.getLastTokenResponse()
            updateClientWithNewToken(tokenResponse!!.idToken!!)
            tokenResponse
        } else {
            auth.getLastTokenResponse()
        }
    }

    override suspend fun <T: Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T> {

        try {
            val tokenResponse = handleTokenRefreshAndReturn()

            val request = Request.newBuilder()
                .uri(resource)
                .header(ACCEPT, if (RDFSource::class.java.isAssignableFrom(clazz)) JSON_LD else ANY)
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

    override suspend fun <T: Resource> create(
        resource: T
    ): SolidNetworkResponse<T> {

        val existingResource = read(resource.getIdentifier(), resource.javaClass)

        if(existingResource is SolidNetworkResponse.Success) {
            return SolidNetworkResponse.Error(409, "Resource already exists")
        } else if (existingResource is SolidNetworkResponse.Error) {
            try {
                val tokenResponse = handleTokenRefreshAndReturn()

                val request: Request = Request.newBuilder()
                    .uri(resource.getIdentifier())
                    .header(CONTENT_TYPE, resource.getContentType())
                    .header(
                        AUTHORIZATION,
                        "${tokenResponse?.tokenType} ${tokenResponse?.accessToken}"
                    )
                    .PUT(Request.BodyPublishers.ofInputStream(resource.getEntity()))
                    .build()

                val response: Response<InputStream> = client.send(
                    request,
                    Response.BodyHandlers.ofInputStream()
                )
                if (response.isSuccessful()) {
                    return SolidNetworkResponse.Success(resource)
                }
                return SolidNetworkResponse.Error(
                    response.statusCode(),
                    response.body().toPlainString()
                )
            } catch (e: Exception) {
                return SolidNetworkResponse.Exception(e)
            }
        } else {
            return SolidNetworkResponse.Error(500, "Unknown error")
        }
    }

    override suspend fun <T: Resource> update(
        newResource: T
    ): SolidNetworkResponse<T> {

        val existingResourceResponse = read(newResource.getIdentifier(), newResource.javaClass)

        when(existingResourceResponse) {
            is SolidNetworkResponse.Success -> {
                try {
                    val tokenResponse = handleTokenRefreshAndReturn()

                    val request: Request = Request.newBuilder()
                        .uri(newResource.getIdentifier())
                        .header(CONTENT_TYPE, newResource.getContentType())
                        .header(
                            AUTHORIZATION,
                            "${tokenResponse?.tokenType} ${tokenResponse?.accessToken}"
                        )
                        .PUT(Request.BodyPublishers.ofInputStream(newResource.getEntity()))
                        .build()

                    val response: Response<String> = client.send(
                        request,
                        Response.BodyHandlers.ofString()
                    )
                    if (response.isSuccessful()) {
                        return SolidNetworkResponse.Success(newResource)
                    }
                    return SolidNetworkResponse.Error(response.statusCode(), response.body())
                } catch (e: Exception) {
                    return SolidNetworkResponse.Exception(e)
                }

            }
            is SolidNetworkResponse.Error -> {
                return SolidNetworkResponse.Error(
                    existingResourceResponse.errorCode,
                    existingResourceResponse.errorMessage
                )
            }
            is SolidNetworkResponse.Exception -> {
                return SolidNetworkResponse.Exception(existingResourceResponse.exception)
            }
        }
    }

    override suspend fun <T: Resource> delete(
        resource: T,
    ): SolidNetworkResponse<T> {

        val existingResourceResponse = read(resource.getIdentifier(), resource.javaClass)

        when (existingResourceResponse) {
            is SolidNetworkResponse.Success -> {
                try {
                    val tokenResponse = handleTokenRefreshAndReturn()

                    val request: Request = Request.newBuilder()
                        .uri(resource.getIdentifier())
                        .header(
                            AUTHORIZATION,
                            "${tokenResponse?.tokenType} ${tokenResponse?.accessToken}"
                        )
                        .DELETE()
                        .build()

                    val response: Response<InputStream> = client.send(
                        request,
                        Response.BodyHandlers.ofInputStream()
                    )

                    if (response.isSuccessful()) {
                        return SolidNetworkResponse.Success(resource)
                    } else {
                        return SolidNetworkResponse.Error(
                            response.statusCode(),
                            response.body().toPlainString()
                        )
                    }
                } catch (e: Exception) {
                    return SolidNetworkResponse.Exception(e)
                }
            }
            else -> {
                return existingResourceResponse
            }
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

            val dataSet = JsonLd
                .toRdf(provideNormalizedDoc(string.byteInputStream()))
                .options(options)
                .get()
            return clazz
                .getConstructor(URI::class.java, MediaType::class.java, RdfDataset::class.java)
                .newInstance(response.uri(), MediaType.of(type), dataSet)
        } else {
            return clazz
                .getConstructor(URI::class.java, String::class.java, InputStream::class.java)
                .newInstance(response.uri(), type, string.byteInputStream())
        }
    }

    private fun provideNormalizedDoc(input: InputStream): JsonDocument {
        val doc = JsonDocument.of(input)
        val newStruct = JsonProvider.instance().createObjectBuilder()
        doc.jsonContent.get().asJsonObject().forEach {
            if (it.key == "@graph") {
                val graphArray = JsonProvider.instance().createArrayBuilder()
                it.value.asJsonArray().forEach {
                    val innerObjectBuilder = JsonProvider.instance().createObjectBuilder()
                    it.asJsonObject().forEach {
                        if (it.key == "@id") {
                            if ((it.value as JsonString).string.startsWith("/")) {
                                innerObjectBuilder.add(it.key, JsonProvider.instance().createValue("https://storage.inrupt.com${(it.value as JsonString).string}"))
                            } else {
                                innerObjectBuilder.add(it.key, it.value)
                            }
                        } else if (it.key == "ldp:contains") {

                            val inArrBuil = JsonProvider.instance().createArrayBuilder()
                            if (it.value is JsonArray) {
                                it.value.asJsonArray().forEach {
                                    val inObjBuilder = JsonProvider.instance().createObjectBuilder()
                                    it.asJsonObject().forEach {
                                        if ((it.value as JsonString).string.startsWith("/")) {
                                            inObjBuilder.add(it.key,
                                                JsonProvider.instance()
                                                    .createValue("https://storage.inrupt.com${(it.value as JsonString).string}")
                                            )
                                        } else {
                                            inObjBuilder.add(it.key, it.value)
                                        }
                                    }
                                    inArrBuil.add(inObjBuilder.build())
                                }
                            } else {
                                it.value.asJsonObject().forEach {
                                    val inObjBuilder = JsonProvider.instance().createObjectBuilder()
                                    if ((it.value as JsonString).string.startsWith("/")) {
                                        inObjBuilder.add(it.key,
                                            JsonProvider.instance()
                                                .createValue("https://storage.inrupt.com${(it.value as JsonString).string}")
                                        )
                                    } else {
                                        inObjBuilder.add(it.key, it.value)
                                    }
                                    inArrBuil.add(inObjBuilder.build())
                                }
                            }
                            innerObjectBuilder.add(it.key, inArrBuil.build())
                        } else {
                            innerObjectBuilder.add(it.key, it.value)
                        }
                    }
                    graphArray.add(innerObjectBuilder.build())
                }
                newStruct.add("@graph", graphArray.build())
            } else {
                newStruct.add(it.key, it.value)
            }
        }
        return JsonDocument.of(newStruct.build().toString().byteInputStream())
    }
}


