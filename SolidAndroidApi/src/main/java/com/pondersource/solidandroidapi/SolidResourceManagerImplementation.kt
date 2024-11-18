package com.pondersource.solidandroidapi

import com.pondersource.solidandroidclient.util.isSuccessful
import com.pondersource.solidandroidclient.util.toPlainString
import java.io.InputStream
import java.net.URI


class SolidResourceManagerImplementation(
    private val auth: Authenticator
) : SolidResourceManager {

    private val client: com.inrupt.client.solid.SolidSyncClient = com.inrupt.client.solid.SolidSyncClient.getClient()

    private fun updateClientWithNewToken(newTokenId: String) {
        client.session(com.inrupt.client.openid.OpenIdSession.ofIdToken(newTokenId))
    }

    private suspend fun handleTokenRefreshAndReturn(): net.openid.appauth.TokenResponse? {
        return if (auth.needsTokenRefresh()) {
            val tokenResponse = auth.getLastTokenResponse()
            updateClientWithNewToken(tokenResponse!!.idToken!!)
            tokenResponse
        } else {
            auth.getLastTokenResponse()
        }
    }

    override suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T> {

        try {
            val tokenResponse = handleTokenRefreshAndReturn()

            val request = com.inrupt.client.Request.newBuilder()
                .uri(resource)
                .header(com.pondersource.solidandroidclient.HTTPHeaderName.ACCEPT, if (com.pondersource.solidandroidclient.RDFSource::class.java.isAssignableFrom(clazz)) com.pondersource.solidandroidclient.HTTPAcceptType.JSON_LD else com.pondersource.solidandroidclient.HTTPAcceptType.ANY)
                .header(com.pondersource.solidandroidclient.HTTPHeaderName.AUTHORIZATION, "${tokenResponse?.tokenType} ${tokenResponse?.idToken}")
                .GET()
                .build()

            val response: com.inrupt.client.Response<InputStream> = client.send(
                request,
                com.inrupt.client.Response.BodyHandlers.ofInputStream()
            )

            return if (response.isSuccessful()) {
                com.pondersource.solidandroidclient.SolidNetworkResponse.Success(constructObject(response, clazz))
            } else {
                com.pondersource.solidandroidclient.SolidNetworkResponse.Error(response.statusCode(), response.body().toPlainString())
            }
        } catch (e: Exception) {
            return com.pondersource.solidandroidclient.SolidNetworkResponse.Exception(e)
        }
    }

    override suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> create(
        resource: T
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T> {

        val existingResource = read(resource.getIdentifier(), resource.javaClass)

        if(existingResource is com.pondersource.solidandroidclient.SolidNetworkResponse.Success) {
            return com.pondersource.solidandroidclient.SolidNetworkResponse.Error(409, "Resource already exists")
        } else if (existingResource is com.pondersource.solidandroidclient.SolidNetworkResponse.Error) {
            try {
                val tokenResponse = handleTokenRefreshAndReturn()

                val request: com.inrupt.client.Request = com.inrupt.client.Request.newBuilder()
                    .uri(resource.getIdentifier())
                    .header(com.pondersource.solidandroidclient.HTTPHeaderName.CONTENT_TYPE, resource.getContentType())
                    .header(
                        com.pondersource.solidandroidclient.HTTPHeaderName.AUTHORIZATION,
                        "${tokenResponse?.tokenType} ${tokenResponse?.idToken}"
                    )
                    .PUT(com.inrupt.client.Request.BodyPublishers.ofInputStream(resource.getEntity()))
                    .build()

                val response: com.inrupt.client.Response<InputStream> = client.send(
                    request,
                    com.inrupt.client.Response.BodyHandlers.ofInputStream()
                )
                if (response.isSuccessful()) {
                    return com.pondersource.solidandroidclient.SolidNetworkResponse.Success(resource)
                }
                return com.pondersource.solidandroidclient.SolidNetworkResponse.Error(
                    response.statusCode(),
                    response.body().toPlainString()
                )
            } catch (e: Exception) {
                return com.pondersource.solidandroidclient.SolidNetworkResponse.Exception(e)
            }
        } else {
            return com.pondersource.solidandroidclient.SolidNetworkResponse.Error(500, "Unknown error")
        }
    }

    override suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> update(
        newResource: T
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T> {

        val existingResourceResponse = read(newResource.getIdentifier(), newResource.javaClass)

        when(existingResourceResponse) {
            is com.pondersource.solidandroidclient.SolidNetworkResponse.Success -> {
                try {
                    val tokenResponse = handleTokenRefreshAndReturn()

                    val request: com.inrupt.client.Request = com.inrupt.client.Request.newBuilder()
                        .uri(newResource.getIdentifier())
                        .header(com.pondersource.solidandroidclient.HTTPHeaderName.CONTENT_TYPE, newResource.getContentType())
                        .header(
                            com.pondersource.solidandroidclient.HTTPHeaderName.AUTHORIZATION,
                            "${tokenResponse?.tokenType} ${tokenResponse?.idToken}"
                        )
                        .PUT(com.inrupt.client.Request.BodyPublishers.ofInputStream(newResource.getEntity()))
                        .build()

                    val response: com.inrupt.client.Response<String> = client.send(
                        request,
                        com.inrupt.client.Response.BodyHandlers.ofString()
                    )
                    if (response.isSuccessful()) {
                        return com.pondersource.solidandroidclient.SolidNetworkResponse.Success(newResource)
                    }
                    return com.pondersource.solidandroidclient.SolidNetworkResponse.Error(response.statusCode(), response.body())
                } catch (e: Exception) {
                    return com.pondersource.solidandroidclient.SolidNetworkResponse.Exception(e)
                }

            }
            is com.pondersource.solidandroidclient.SolidNetworkResponse.Error -> {
                return com.pondersource.solidandroidclient.SolidNetworkResponse.Error(
                    existingResourceResponse.errorCode,
                    existingResourceResponse.errorMessage
                )
            }
            is com.pondersource.solidandroidclient.SolidNetworkResponse.Exception -> {
                return com.pondersource.solidandroidclient.SolidNetworkResponse.Exception(existingResourceResponse.exception)
            }
        }
    }

    override suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> delete(
        resource: T,
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T> {

        val existingResourceResponse = read(resource.getIdentifier(), resource.javaClass)

        when (existingResourceResponse) {
            is com.pondersource.solidandroidclient.SolidNetworkResponse.Success -> {
                try {
                    val tokenResponse = handleTokenRefreshAndReturn()

                    val request: com.inrupt.client.Request = com.inrupt.client.Request.newBuilder()
                        .uri(resource.getIdentifier())
                        .header(
                            com.pondersource.solidandroidclient.HTTPHeaderName.AUTHORIZATION,
                            "${tokenResponse?.tokenType} ${tokenResponse?.idToken}"
                        )
                        .DELETE()
                        .build()

                    val response: com.inrupt.client.Response<InputStream> = client.send(
                        request,
                        com.inrupt.client.Response.BodyHandlers.ofInputStream()
                    )

                    if (response.isSuccessful()) {
                        return com.pondersource.solidandroidclient.SolidNetworkResponse.Success(resource)
                    } else {
                        return com.pondersource.solidandroidclient.SolidNetworkResponse.Error(
                            response.statusCode(),
                            response.body().toPlainString()
                        )
                    }
                } catch (e: Exception) {
                    return com.pondersource.solidandroidclient.SolidNetworkResponse.Exception(e)
                }
            }
            else -> {
                return existingResourceResponse
            }
        }
    }

    private fun <T> constructObject(
        response: com.inrupt.client.Response<InputStream>,
        clazz: Class<T>
    ): T {
        val type = response.headers().firstValue(com.pondersource.solidandroidclient.HTTPHeaderName.CONTENT_TYPE)
            .orElse(com.pondersource.solidandroidclient.HTTPAcceptType.OCTET_STREAM)
        val string = response.body().toPlainString()
        if (com.pondersource.solidandroidclient.RDFSource::class.java.isAssignableFrom(clazz)) {
            val options = com.apicatalog.jsonld.JsonLdOptions().apply {
                isRdfStar = true
            }

            val dataSet = com.apicatalog.jsonld.JsonLd
                .toRdf(provideNormalizedDoc(string.byteInputStream()))
                .options(options)
                .get()
            return clazz
                .getConstructor(URI::class.java, com.apicatalog.jsonld.http.media.MediaType::class.java, com.apicatalog.rdf.RdfDataset::class.java)
                .newInstance(response.uri(), com.apicatalog.jsonld.http.media.MediaType.of(type), dataSet)
        } else {
            return clazz
                .getConstructor(URI::class.java, String::class.java, InputStream::class.java)
                .newInstance(response.uri(), type, string.byteInputStream())
        }
    }

    private fun provideNormalizedDoc(input: InputStream): com.apicatalog.jsonld.document.JsonDocument {
        val doc = com.apicatalog.jsonld.document.JsonDocument.of(input)
        val newStruct = com.apicatalog.jsonld.json.JsonProvider.instance().createObjectBuilder()
        doc.jsonContent.get().asJsonObject().forEach {
            if (it.key == "@graph") {
                val graphArray = com.apicatalog.jsonld.json.JsonProvider.instance().createArrayBuilder()
                it.value.asJsonArray().forEach {
                    val innerObjectBuilder = com.apicatalog.jsonld.json.JsonProvider.instance().createObjectBuilder()
                    it.asJsonObject().forEach {
                        if (it.key == "@id") {
                            if ((it.value as jakarta.json.JsonString).string.startsWith("/")) {
                                innerObjectBuilder.add(it.key, com.apicatalog.jsonld.json.JsonProvider.instance().createValue("https://storage.inrupt.com${(it.value as jakarta.json.JsonString).string}"))
                            } else {
                                innerObjectBuilder.add(it.key, it.value)
                            }
                        } else if (it.key == "ldp:contains") {

                            val inArrBuil = com.apicatalog.jsonld.json.JsonProvider.instance().createArrayBuilder()
                            if (it.value is jakarta.json.JsonArray) {
                                it.value.asJsonArray().forEach {
                                    val inObjBuilder = com.apicatalog.jsonld.json.JsonProvider.instance().createObjectBuilder()
                                    it.asJsonObject().forEach {
                                        if ((it.value as jakarta.json.JsonString).string.startsWith("/")) {
                                            inObjBuilder.add(it.key,
                                                com.apicatalog.jsonld.json.JsonProvider.instance()
                                                    .createValue("https://storage.inrupt.com${(it.value as jakarta.json.JsonString).string}")
                                            )
                                        } else {
                                            inObjBuilder.add(it.key, it.value)
                                        }
                                    }
                                    inArrBuil.add(inObjBuilder.build())
                                }
                            } else {
                                it.value.asJsonObject().forEach {
                                    val inObjBuilder = com.apicatalog.jsonld.json.JsonProvider.instance().createObjectBuilder()
                                    if ((it.value as jakarta.json.JsonString).string.startsWith("/")) {
                                        inObjBuilder.add(it.key,
                                            com.apicatalog.jsonld.json.JsonProvider.instance()
                                                .createValue("https://storage.inrupt.com${(it.value as jakarta.json.JsonString).string}")
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
        return com.apicatalog.jsonld.document.JsonDocument.of(newStruct.build().toString().byteInputStream())
    }
}


