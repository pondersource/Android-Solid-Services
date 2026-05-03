package com.pondersource.solidandroidapi.resource.implementation

import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.Resource
import com.pondersource.shared.domain.resource.SolidContainer
import com.pondersource.shared.domain.resource.SolidMetadata
import com.pondersource.shared.domain.resource.SolidNonRDFResource
import com.pondersource.shared.vocab.LDP
import com.pondersource.solidandroidapi.auth.Authenticator
import com.pondersource.solidandroidapi.resource.SolidResourceManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.net.URI

internal class SolidResourceManagerImplementation : SolidResourceManager {

    companion object {
        @Volatile
        private var INSTANCE: SolidResourceManager? = null

        internal fun getInstance(authenticator: Authenticator): SolidResourceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SolidResourceManagerImplementation(authenticator).also { INSTANCE = it }
            }
        }
    }

    private val solidHttpClient: SolidHttpClient

    private constructor(authenticator: Authenticator) {
        solidHttpClient = SolidHttpClient(authenticator)
    }

    override suspend fun head(
        webid: String,
        uri: URI,
    ): SolidNetworkResponse<SolidMetadata> = solidHttpClient.head(webid, uri)

    override suspend fun <T : Resource> read(
        webid: String,
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T> {
        val result = solidHttpClient.get(webid, resource, clazz)
        if (result is SolidNetworkResponse.Success && result.data is SolidContainer) {
            val container = result.data as SolidContainer
            val enriched = coroutineScope {
                container.getContained().map { ref ->
                    async {
                        when (val headResult =
                            solidHttpClient.head(webid, URI.create(ref.identifier))) {
                            is SolidNetworkResponse.Success -> ref.copy(headMetadata = headResult.data)
                            else -> ref
                        }
                    }
                }.awaitAll()
            }
            container.enrichContained(enriched)
        }
        return result
    }

    override suspend fun <T : Resource> create(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T> {
        return try {
            val response = solidHttpClient.put(webid, resource, ifNoneMatchStar = true)
            if (response is SolidNetworkResponse.Error && response.errorCode == 412) {
                SolidNetworkResponse.Error(409, "Resource already exists")
            } else {
                response
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    override suspend fun <T : Resource> update(
        webid: String,
        newResource: T,
        ifMatch: String?,
    ): SolidNetworkResponse<T> {
        return try {
            val response = solidHttpClient.put(webid, newResource, ifMatch = ifMatch ?: "*")
            if (response is SolidNetworkResponse.Error && response.errorCode == 412) {
                SolidNetworkResponse.Error(404, "Resource not found")
            } else {
                response
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    override suspend fun patch(
        webid: String,
        uri: URI,
        patch: N3Patch,
        ifMatch: String?,
    ): SolidNetworkResponse<Unit> = solidHttpClient.patch(webid, uri, patch, ifMatch)

    override suspend fun patchRaw(
        webid: String,
        uri: URI,
        n3Body: String,
        ifMatch: String?,
    ): SolidNetworkResponse<Unit> = solidHttpClient.patchRaw(webid, uri, n3Body, ifMatch)

    override suspend fun <T : Resource> delete(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T> {
        return try {
            val uri = resource.getIdentifier()
            val deleteResult = if (resource is SolidContainer || uri.toString().endsWith("/")) {
                deleteRecursive(webid, uri)
            } else {
                solidHttpClient.delete(webid, uri)
            }
            when (deleteResult) {
                is SolidNetworkResponse.Success -> SolidNetworkResponse.Success(resource)
                is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(
                    deleteResult.errorCode,
                    deleteResult.errorMessage
                )

                is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(deleteResult.exception)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    override suspend fun delete(
        webid: String,
        resourceUri: URI,
    ): SolidNetworkResponse<Boolean> {
        return try {
            if (resourceUri.toString().endsWith("/")) {
                deleteRecursive(webid, resourceUri)
            } else {
                solidHttpClient.delete(webid, resourceUri)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    private suspend fun <T : Resource> move(
        webid: String,
        resource: T,
        destinationUri: URI,
    ): SolidNetworkResponse<T> {
        val sourceUri = resource.getIdentifier()
        if (sourceUri == destinationUri) return SolidNetworkResponse.Success(resource)
        return try {
            val moveResult = move(webid, sourceUri, destinationUri)
            @Suppress("UNCHECKED_CAST")
            when (moveResult) {
                is SolidNetworkResponse.Success -> solidHttpClient.get(webid, destinationUri, resource.javaClass) as SolidNetworkResponse<T>
                is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(moveResult.errorCode, moveResult.errorMessage)
                is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(moveResult.exception)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    private suspend fun move(
        webid: String,
        resourceUri: URI,
        destinationUri: URI,
    ): SolidNetworkResponse<Boolean> {
        if (resourceUri == destinationUri) return SolidNetworkResponse.Success(true)
        return try {
            val copyResult = if (resourceUri.toString().endsWith("/")) {
                copyContainerRecursive(webid, resourceUri, destinationUri)
            } else {
                copyResource(webid, resourceUri, destinationUri)
            }
            if (copyResult !is SolidNetworkResponse.Success) return copyResult

            if (resourceUri.toString().endsWith("/")) {
                deleteRecursive(webid, resourceUri)
            } else {
                solidHttpClient.delete(webid, resourceUri)
            }
        } catch (e: Exception) {
            SolidNetworkResponse.Exception(e)
        }
    }

    private suspend fun copyResource(
        webid: String,
        sourceUri: URI,
        destinationUri: URI,
    ): SolidNetworkResponse<Boolean> {
        val copyResult = solidHttpClient.copy(webid, sourceUri, destinationUri)
        if (copyResult is SolidNetworkResponse.Success) return copyResult
        if (copyResult is SolidNetworkResponse.Error && copyResult.errorCode != 405) return copyResult

        val getResult = solidHttpClient.get(webid, sourceUri, SolidNonRDFResource::class.java)
        if (getResult !is SolidNetworkResponse.Success) {
            return when (getResult) {
                is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(getResult.errorCode, getResult.errorMessage)
                is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(getResult.exception)
            }
        }
        val source = getResult.data
        val destResource = SolidNonRDFResource(destinationUri, source.getContentType(), source.getEntity(), null)
        val putResult = solidHttpClient.put(webid, destResource, ifNoneMatchStar = true)
        return when (putResult) {
            is SolidNetworkResponse.Success -> SolidNetworkResponse.Success(true)
            is SolidNetworkResponse.Error -> {
                val code = if (putResult.errorCode == 412) 409 else putResult.errorCode
                SolidNetworkResponse.Error(code, putResult.errorMessage)
            }
            is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(putResult.exception)
        }
    }

    private suspend fun copyContainerRecursive(
        webid: String,
        sourceUri: URI,
        destinationUri: URI,
    ): SolidNetworkResponse<Boolean> {
        val copyResult = solidHttpClient.copy(webid, sourceUri, destinationUri)
        if (copyResult is SolidNetworkResponse.Success) return copyResult
        if (copyResult is SolidNetworkResponse.Error && copyResult.errorCode != 405) return copyResult

        val putResult = solidHttpClient.put(webid, SolidContainer(destinationUri), ifNoneMatchStar = true)
        if (putResult is SolidNetworkResponse.Error && putResult.errorCode != 412) {
            return SolidNetworkResponse.Error(putResult.errorCode, putResult.errorMessage)
        }
        if (putResult is SolidNetworkResponse.Exception) return SolidNetworkResponse.Exception(putResult.exception)

        val containerResult = solidHttpClient.get(webid, sourceUri, SolidContainer::class.java)
        if (containerResult !is SolidNetworkResponse.Success) {
            return when (containerResult) {
                is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(containerResult.errorCode, containerResult.errorMessage)
                is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(containerResult.exception)
            }
        }

        coroutineScope {
            containerResult.data.getContained().map { ref ->
                async {
                    val childUri = URI.create(ref.identifier)
                    val relativePath = ref.identifier.removePrefix(sourceUri.toString())
                    val childDest = URI.create(destinationUri.toString() + relativePath)
                    val isChildContainer = ref.isContainerByUri() ||
                            ref.types.contains(LDP.BASIC_CONTAINER) ||
                            ref.types.contains(LDP.CONTAINER) ||
                            ref.types.contains(LDP.DIRECT_CONTAINER) ||
                            ref.types.contains(LDP.INDIRECT_CONTAINER)
                    if (isChildContainer) {
                        copyContainerRecursive(webid, childUri, childDest).getOrThrow()
                    } else {
                        copyResource(webid, childUri, childDest).getOrThrow()
                    }
                }
            }.awaitAll()
        }

        return SolidNetworkResponse.Success(true)
    }

    private suspend fun deleteRecursive(
        webid: String,
        containerUri: URI
    ): SolidNetworkResponse<Boolean> {
        val containerResult = solidHttpClient.get(webid, containerUri, SolidContainer::class.java)
        if (containerResult !is SolidNetworkResponse.Success) {
            return when (containerResult) {
                is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(
                    containerResult.errorCode,
                    containerResult.errorMessage
                )

                is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(containerResult.exception)
            }
        }
        coroutineScope {
            containerResult.data.getContained().map { ref ->
                async {
                    val isChildContainer = ref.isContainerByUri() ||
                            ref.types.contains(LDP.BASIC_CONTAINER) ||
                            ref.types.contains(LDP.CONTAINER) ||
                            ref.types.contains(LDP.DIRECT_CONTAINER) ||
                            ref.types.contains(LDP.INDIRECT_CONTAINER)
                    if (isChildContainer) {
                        deleteRecursive(webid, URI.create(ref.identifier)).getOrThrow()
                    } else {
                        solidHttpClient.delete(webid, URI.create(ref.identifier)).getOrThrow()
                    }
                }
            }.awaitAll()
        }

        return solidHttpClient.delete(webid, containerUri)
    }
}
