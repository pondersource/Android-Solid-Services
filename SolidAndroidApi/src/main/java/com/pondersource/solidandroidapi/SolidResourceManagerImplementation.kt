package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.domain.container.SolidContainer
import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.Resource
import com.pondersource.shared.vocab.LDP
import java.net.URI

internal class SolidResourceManagerImplementation : SolidResourceManager {

    companion object {
        @Volatile
        private var INSTANCE: SolidResourceManager? = null

        fun getInstance(context: Context): SolidResourceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SolidResourceManagerImplementation(context).also { INSTANCE = it }
            }
        }
    }

    private val solidHttpClient: SolidHttpClient

    private constructor(context: Context) {
        solidHttpClient = SolidHttpClient(AuthenticatorImplementation.getInstance(context))
    }

    override suspend fun <T : Resource> read(
        webid: String,
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T> = solidHttpClient.get(webid, resource, clazz)

    override suspend fun <T : Resource> create(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T> {
        val existing = solidHttpClient.get(webid, resource.getIdentifier(), resource.javaClass)
        return when {
            existing is SolidNetworkResponse.Success -> SolidNetworkResponse.Error(
                409,
                "Resource already exists"
            )

            existing is SolidNetworkResponse.Error -> solidHttpClient.put(webid, resource)
            else -> SolidNetworkResponse.Error(500, "Unknown error")
        }
    }

    override suspend fun <T : Resource> update(
        webid: String,
        newResource: T,
        ifMatch: String?,
    ): SolidNetworkResponse<T> = solidHttpClient.put(webid, newResource, ifMatch)

    override suspend fun patch(
        webid: String,
        uri: URI,
        patch: N3Patch,
    ): SolidNetworkResponse<Unit> = solidHttpClient.patch(webid, uri, patch)

    override suspend fun patchRaw(
        webid: String,
        uri: URI,
        n3Body: String,
    ): SolidNetworkResponse<Unit> = solidHttpClient.patchRaw(webid, uri, n3Body)

    override suspend fun <T : Resource> delete(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T> {
        val existing = solidHttpClient.get(webid, resource.getIdentifier(), resource.javaClass)
        return when (existing) {
            is SolidNetworkResponse.Success -> {
                when (val del = solidHttpClient.delete(webid, resource.getIdentifier())) {
                    is SolidNetworkResponse.Success -> SolidNetworkResponse.Success(resource)
                    is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(
                        del.errorCode,
                        del.errorMessage
                    )

                    is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(del.exception)
                }
            }

            else -> existing
        }
    }

    override suspend fun delete(
        webid: String,
        resourceUri: URI,
    ): SolidNetworkResponse<Boolean> {
        return when (val del = solidHttpClient.delete(webid, resourceUri)) {
            is SolidNetworkResponse.Success -> SolidNetworkResponse.Success(true)
            is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(
                del.errorCode,
                del.errorMessage
            )

            is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(del.exception)
        }
    }

    override suspend fun deleteContainer(
        webid: String,
        containerUri: URI,
    ): SolidNetworkResponse<Boolean> {
        if (!containerUri.toString().endsWith("/")) {
            return SolidNetworkResponse.Error(400, "Container URL must end with /")
        }
        val container =
            solidHttpClient.get(webid, containerUri, SolidContainer::class.java).getOrThrow()
        container.getContained().forEach {
            if (it.types.contains(LDP.BASIC_CONTAINER)) {
                deleteContainer(webid, URI.create(it.identifier)).getOrThrow()
            } else {
                solidHttpClient.delete(webid, URI.create(it.identifier)).getOrThrow()
            }
        }
        solidHttpClient.delete(webid, containerUri).getOrThrow()
        return SolidNetworkResponse.Success(true)
    }
}
