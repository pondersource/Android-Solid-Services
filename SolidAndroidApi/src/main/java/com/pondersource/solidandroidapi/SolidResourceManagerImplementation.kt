package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.domain.container.SolidContainer
import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.Resource
import com.pondersource.shared.domain.resource.SolidMetadata
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
            val enriched = container.getContained().map { ref ->
                when (val headResult = solidHttpClient.head(webid, URI.create(ref.identifier))) {
                    is SolidNetworkResponse.Success -> ref.copy(headMetadata = headResult.data)
                    else -> ref
                }
            }
            container.enrichContained(enriched)
        }
        return result
    }

    override suspend fun <T : Resource> create(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T> {
        val head = solidHttpClient.head(webid, resource.getIdentifier())
        when {
            head is SolidNetworkResponse.Success -> {
                // Benefit 1: Existence check — resource already exists
                return SolidNetworkResponse.Error(409, "Resource already exists")
            }

            head is SolidNetworkResponse.Error && head.errorCode == 403 -> {
                // Benefit 2: Permission pre-check — server denies even HEAD
                return SolidNetworkResponse.Error(
                    403,
                    "Insufficient permissions to create resource"
                )
            }

            head is SolidNetworkResponse.Error && head.errorCode == 404 -> {
                // Resource does not exist — proceed to create
            }

            head is SolidNetworkResponse.Exception -> {
                return SolidNetworkResponse.Exception(head.exception)
            }
        }
        return solidHttpClient.put(webid, resource)
    }

    override suspend fun <T : Resource> update(
        webid: String,
        newResource: T,
        ifMatch: String?,
    ): SolidNetworkResponse<T> {
        val resolvedIfMatch: String?
        if (ifMatch != null) {
            resolvedIfMatch = ifMatch
        } else {
            val head = solidHttpClient.head(webid, newResource.getIdentifier())
            when {
                head is SolidNetworkResponse.Error && head.errorCode == 404 -> {
                    return SolidNetworkResponse.Error(404, "Resource not found")
                }

                head is SolidNetworkResponse.Error && head.errorCode == 403 -> {
                    // Benefit 2: Permission pre-check
                    return SolidNetworkResponse.Error(
                        403,
                        "Insufficient permissions to update resource"
                    )
                }

                head is SolidNetworkResponse.Exception -> {
                    return SolidNetworkResponse.Exception(head.exception)
                }

                head is SolidNetworkResponse.Success -> {
                    val meta = head.data
                    // Benefit 2: Permission pre-check via WAC-Allow
                    meta.wacAllow?.let {
                        if (!it.canWrite()) return SolidNetworkResponse.Error(
                            403,
                            "Insufficient permissions to update resource"
                        )
                    }
                    // Benefit 6: Method capability check
                    if (meta.allowedMethods.isNotEmpty() && "PUT" !in meta.allowedMethods) {
                        return SolidNetworkResponse.Error(
                            405,
                            "PUT method not allowed on this resource"
                        )
                    }
                    // Benefit 5: ETag for conditional PUT — prevents lost-update races
                    resolvedIfMatch = meta.etag
                }

                else -> resolvedIfMatch = null
            }
        }
        return solidHttpClient.put(webid, newResource, resolvedIfMatch)
    }

    override suspend fun patch(
        webid: String,
        uri: URI,
        patch: N3Patch,
    ): SolidNetworkResponse<Unit> {
        val head = solidHttpClient.head(webid, uri)
        val etag: String?
        when {
            head is SolidNetworkResponse.Error && head.errorCode == 404 -> {
                return SolidNetworkResponse.Error(404, "Resource not found")
            }

            head is SolidNetworkResponse.Error && head.errorCode == 403 -> {
                return SolidNetworkResponse.Error(403, "Insufficient permissions to patch resource")
            }

            head is SolidNetworkResponse.Exception -> {
                return SolidNetworkResponse.Exception(head.exception)
            }

            head is SolidNetworkResponse.Success -> {
                val meta = head.data
                // Benefit 2: Permission pre-check via WAC-Allow
                meta.wacAllow?.let {
                    if (!it.canWrite()) return SolidNetworkResponse.Error(
                        403,
                        "Insufficient permissions to patch resource"
                    )
                }
                // Benefit 6: Method capability check
                if (meta.allowedMethods.isNotEmpty() && "PATCH" !in meta.allowedMethods) {
                    return SolidNetworkResponse.Error(
                        405,
                        "PATCH method not allowed on this resource"
                    )
                }
                // Benefit 5: ETag for conditional PATCH
                etag = meta.etag
            }

            else -> etag = null
        }
        return solidHttpClient.patch(webid, uri, patch, etag)
    }

    override suspend fun patchRaw(
        webid: String,
        uri: URI,
        n3Body: String,
    ): SolidNetworkResponse<Unit> {
        val head = solidHttpClient.head(webid, uri)
        val etag: String?
        when {
            head is SolidNetworkResponse.Error && head.errorCode == 404 -> {
                return SolidNetworkResponse.Error(404, "Resource not found")
            }

            head is SolidNetworkResponse.Error && head.errorCode == 403 -> {
                return SolidNetworkResponse.Error(403, "Insufficient permissions to patch resource")
            }

            head is SolidNetworkResponse.Exception -> {
                return SolidNetworkResponse.Exception(head.exception)
            }

            head is SolidNetworkResponse.Success -> {
                val meta = head.data
                meta.wacAllow?.let {
                    if (!it.canWrite()) return SolidNetworkResponse.Error(
                        403,
                        "Insufficient permissions to patch resource"
                    )
                }
                if (meta.allowedMethods.isNotEmpty() && "PATCH" !in meta.allowedMethods) {
                    return SolidNetworkResponse.Error(
                        405,
                        "PATCH method not allowed on this resource"
                    )
                }
                etag = meta.etag
            }

            else -> etag = null
        }
        return solidHttpClient.patchRaw(webid, uri, n3Body, etag)
    }

    override suspend fun <T : Resource> delete(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T> {
        val head = solidHttpClient.head(webid, resource.getIdentifier())
        return when {
            head is SolidNetworkResponse.Error && head.errorCode == 404 -> {
                // Benefit 1: Existence check — resource does not exist
                SolidNetworkResponse.Error(404, "Resource not found")
            }

            head is SolidNetworkResponse.Error && head.errorCode == 403 -> {
                // Benefit 2: Permission pre-check
                SolidNetworkResponse.Error(403, "Insufficient permissions to delete resource")
            }

            head is SolidNetworkResponse.Exception -> {
                SolidNetworkResponse.Exception(head.exception)
            }

            head is SolidNetworkResponse.Success -> {
                val meta = head.data
                // Benefit 2: Permission pre-check via WAC-Allow
                meta.wacAllow?.let {
                    if (!it.canWrite()) return SolidNetworkResponse.Error(
                        403,
                        "Insufficient permissions to delete resource"
                    )
                }
                // Benefit 6: Method capability check
                if (meta.allowedMethods.isNotEmpty() && "DELETE" !in meta.allowedMethods) {
                    return SolidNetworkResponse.Error(
                        405,
                        "DELETE method not allowed on this resource"
                    )
                }
                // Benefit 5: ETag for conditional DELETE
                when (val del =
                    solidHttpClient.delete(webid, resource.getIdentifier(), meta.etag)) {
                    is SolidNetworkResponse.Success -> SolidNetworkResponse.Success(resource)
                    is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(
                        del.errorCode,
                        del.errorMessage
                    )

                    is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(del.exception)
                }
            }

            else -> SolidNetworkResponse.Error(500, "Unknown error")
        }
    }

    override suspend fun delete(
        webid: String,
        resourceUri: URI,
    ): SolidNetworkResponse<Boolean> {
        val head = solidHttpClient.head(webid, resourceUri)
        return when {
            head is SolidNetworkResponse.Error && head.errorCode == 404 -> {
                SolidNetworkResponse.Error(404, "Resource not found")
            }

            head is SolidNetworkResponse.Error && head.errorCode == 403 -> {
                SolidNetworkResponse.Error(403, "Insufficient permissions to delete resource")
            }

            head is SolidNetworkResponse.Exception -> {
                SolidNetworkResponse.Exception(head.exception)
            }

            head is SolidNetworkResponse.Success -> {
                val meta = head.data
                meta.wacAllow?.let {
                    if (!it.canWrite()) return SolidNetworkResponse.Error(
                        403,
                        "Insufficient permissions to delete resource"
                    )
                }
                if (meta.allowedMethods.isNotEmpty() && "DELETE" !in meta.allowedMethods) {
                    return SolidNetworkResponse.Error(
                        405,
                        "DELETE method not allowed on this resource"
                    )
                }
                when (val del = solidHttpClient.delete(webid, resourceUri, meta.etag)) {
                    is SolidNetworkResponse.Success -> SolidNetworkResponse.Success(true)
                    is SolidNetworkResponse.Error -> SolidNetworkResponse.Error(
                        del.errorCode,
                        del.errorMessage
                    )

                    is SolidNetworkResponse.Exception -> SolidNetworkResponse.Exception(del.exception)
                }
            }

            else -> SolidNetworkResponse.Error(500, "Unknown error")
        }
    }

    override suspend fun deleteContainer(
        webid: String,
        containerUri: URI,
    ): SolidNetworkResponse<Boolean> {
        if (!containerUri.toString().endsWith("/")) {
            return SolidNetworkResponse.Error(400, "Container URL must end with /")
        }
        val head = solidHttpClient.head(webid, containerUri)
        val etag: String?
        when {
            head is SolidNetworkResponse.Error && head.errorCode == 404 -> {
                return SolidNetworkResponse.Error(404, "Container not found")
            }

            head is SolidNetworkResponse.Error && head.errorCode == 403 -> {
                return SolidNetworkResponse.Error(
                    403,
                    "Insufficient permissions to delete container"
                )
            }

            head is SolidNetworkResponse.Exception -> {
                return SolidNetworkResponse.Exception(head.exception)
            }

            head is SolidNetworkResponse.Success -> {
                val meta = head.data
                meta.wacAllow?.let {
                    if (!it.canWrite()) return SolidNetworkResponse.Error(
                        403,
                        "Insufficient permissions to delete container"
                    )
                }
                if (meta.allowedMethods.isNotEmpty() && "DELETE" !in meta.allowedMethods) {
                    return SolidNetworkResponse.Error(
                        405,
                        "DELETE method not allowed on this container"
                    )
                }
                etag = meta.etag
            }

            else -> etag = null
        }
        val container = read(webid, containerUri, SolidContainer::class.java).getOrThrow()
        container.getContained().forEach {
            val isContainer = it.headMetadata?.linkTypes?.any { type ->
                type.toString() == LDP.BASIC_CONTAINER ||
                        type.toString() == LDP.CONTAINER ||
                        type.toString() == LDP.DIRECT_CONTAINER ||
                        type.toString() == LDP.INDIRECT_CONTAINER
            } ?: it.types.contains(LDP.BASIC_CONTAINER) || it.isContainerByUri()
            if (isContainer) {
                deleteContainer(webid, URI.create(it.identifier)).getOrThrow()
            } else {
                solidHttpClient.delete(webid, URI.create(it.identifier), it.headMetadata?.etag)
                    .getOrThrow()
            }
        }
        solidHttpClient.delete(webid, containerUri, etag).getOrThrow()
        return SolidNetworkResponse.Success(true)
    }
}
