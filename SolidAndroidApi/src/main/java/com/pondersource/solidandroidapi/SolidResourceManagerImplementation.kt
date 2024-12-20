package com.pondersource.solidandroidapi

import android.content.Context
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.JsonLdVersion
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.jsonld.json.JsonProvider
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidSyncClient
import com.pondersource.shared.HTTPAcceptType
import com.pondersource.shared.HTTPHeaderName
import com.pondersource.shared.RDFSource
import com.pondersource.shared.SolidNetworkResponse
import com.pondersource.shared.resource.Resource
import com.pondersource.shared.resource.SolidContainer
import com.pondersource.shared.util.isSuccessful
import com.pondersource.shared.util.toPlainString
import com.pondersource.shared.vocab.LDP
import jakarta.json.JsonString
import net.openid.appauth.TokenResponse
import okhttp3.Headers
import java.io.InputStream
import java.net.URI

class SolidResourceManagerImplementation: SolidResourceManager {

    companion object {
        @Volatile
        private lateinit var INSTANCE: SolidResourceManager

        fun getInstance(
            context: Context,
        ): SolidResourceManager {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidResourceManagerImplementation(context)
                INSTANCE
            }
        }
    }

    private val auth: Authenticator
    private val client: SolidSyncClient = SolidSyncClient.getClient()

    private constructor(context: Context) {
        this.auth = AuthenticatorImplementation.getInstance(context)
    }

    private fun updateClientWithNewToken(newTokenId: String) {
        client.session(OpenIdSession.ofIdToken(newTokenId))
    }

    private suspend fun handleTokenRefreshAndReturn(): TokenResponse? {

        if (auth.getProfile().userInfo == null) {
            throw IllegalArgumentException("Auth object should be authenticated before interacting with resources.")
        }

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
                .header(HTTPHeaderName.ACCEPT, if (RDFSource::class.java.isAssignableFrom(clazz)) HTTPAcceptType.JSON_LD else HTTPAcceptType.ANY)
                .header(HTTPHeaderName.AUTHORIZATION, "${tokenResponse?.tokenType} ${tokenResponse?.idToken}")
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
            return rawCreate(resource)
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
                val deleteResponse = delete(existingResourceResponse.data)
                when(deleteResponse) {
                    is SolidNetworkResponse.Success -> {
                        return rawCreate(newResource)
                    }
                    is SolidNetworkResponse.Error -> {
                        return SolidNetworkResponse.Error(
                            deleteResponse.errorCode,
                            deleteResponse.errorMessage
                        )
                    }
                    is SolidNetworkResponse.Exception -> {
                        return SolidNetworkResponse.Exception(deleteResponse.exception)
                    }
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
                return rawDelete(resource)
            }
            else -> {
                return existingResourceResponse
            }
        }
    }

    override suspend fun deleteContainer(containerUri: URI): SolidNetworkResponse<Boolean> {
        if(containerUri.toString().endsWith("/")) {
            val containerRdf = read(containerUri, SolidContainer::class.java).handleResponse()
            containerRdf.getContained().forEach {
                if(it.types.contains(LDP.BasicContainer.toString())) {
                    deleteContainer(URI.create(it.identifier)).handleResponse()
                } else {
                    rawDelete(URI.create(it.identifier)).handleResponse()
                }
            }
            rawDelete(containerUri).handleResponse()
            return SolidNetworkResponse.Success(true)
        } else {
            return SolidNetworkResponse.Error(400, "Container URL must end with /")
        }
    }

    private suspend fun <T: Resource> rawCreate(
        resource: T
    ): SolidNetworkResponse<T> {
        try {
            val tokenResponse = handleTokenRefreshAndReturn()

            val type = if (SolidContainer::class.java.isAssignableFrom(resource.javaClass)) {
                "<${LDP.BasicContainer}>; rel=\"type\""
            } else if (RDFSource::class.java.isAssignableFrom(resource.javaClass)) {
                "<${LDP.RDFSource}>; rel=\"type\""
            } else {
                "<${LDP.NonRDFSource}>; rel=\"type\""
            }

            val request: Request = Request.newBuilder()
                .uri(resource.getIdentifier())
                .type(resource.getContentType())
                .header(HTTPHeaderName.LINK, type)
                .header(HTTPHeaderName.ACCEPT, resource.getContentType())
                .header(
                    HTTPHeaderName.AUTHORIZATION,
                    "${tokenResponse?.tokenType} ${tokenResponse?.idToken}"
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
    }

    private suspend fun <T: Resource> rawDelete(
        resource: T
    ): SolidNetworkResponse<T> {
        try {
            val tokenResponse = handleTokenRefreshAndReturn()

            val request: Request = Request.newBuilder()
                .uri(resource.getIdentifier())
                .header(
                    HTTPHeaderName.AUTHORIZATION,
                    "${tokenResponse?.tokenType} ${tokenResponse?.idToken}"
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

    private suspend fun rawDelete(
        uri: URI
    ): SolidNetworkResponse<Boolean> {
        try {
            val tokenResponse = handleTokenRefreshAndReturn()

            val request: Request = Request.newBuilder()
                .uri(uri)
                .header(
                    HTTPHeaderName.AUTHORIZATION,
                    "${tokenResponse?.tokenType} ${tokenResponse?.idToken}"
                )
                .DELETE()
                .build()

            val response: Response<InputStream> = client.send(
                request,
                Response.BodyHandlers.ofInputStream()
            )

            if (response.isSuccessful()) {
                return SolidNetworkResponse.Success(true)
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

    private fun <T> constructObject(
        response: Response<InputStream>,
        clazz: Class<T>
    ): T {
        val type = response.headers().firstValue(HTTPHeaderName.CONTENT_TYPE)
            .orElse(HTTPAcceptType.OCTET_STREAM)
        val string = response.body().toPlainString()
        if (SolidContainer::class.java.isAssignableFrom(clazz)) {
            val dataSet = JsonLd
                .toRdf(provideNormalizedDoc(string.byteInputStream()))
                .get()
            return clazz
                .getConstructor(URI::class.java, MediaType::class.java, RdfDataset::class.java,
                    Headers::class.java)
                .newInstance(response.uri(), MediaType.of(type), dataSet, null)
        }
        else if (RDFSource::class.java.isAssignableFrom(clazz)) {
            val dataSet = JsonLd
                .toRdf(JsonDocument.of(string.byteInputStream()))
                .rdfDirection(JsonLdOptions.RdfDirection.I18N_DATATYPE)
                .mode(JsonLdVersion.V1_1)
                .produceGeneralizedRdf()
                .get()
            return clazz
                .getConstructor(
                    URI::class.java,
                    MediaType::class.java,
                    RdfDataset::class.java,
                    Headers::class.java
                ).newInstance(
                    response.uri(),
                    MediaType.of(type),
                    dataSet,
                    null
                )
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
                            if (it.value is jakarta.json.JsonArray) {
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


