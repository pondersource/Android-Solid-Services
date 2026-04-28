package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.Resource
import java.net.URI

/**
 * Performs authenticated CRUD operations on Solid pod resources on behalf of a specific user.
 *
 * All operations require the user identified by `webid` to have an active, authorized
 * [Authenticator] session.  Results are wrapped in [SolidNetworkResponse] so callers can
 * distinguish HTTP errors from unexpected exceptions without catching throwables.
 *
 * Obtain an instance via [SolidResourceManager.getInstance].
 */
interface SolidResourceManager {

    companion object {
        /**
         * Returns the application-scoped singleton [SolidResourceManager].
         * @param context Any [Context]; the application context is used internally.
         */
        fun getInstance(context: Context): SolidResourceManager = SolidResourceManagerImplementation.getInstance(context)
    }

    /**
     * Reads a resource from the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The URI of the resource to read.
     * @param clazz The expected resource type (e.g. [com.pondersource.shared.domain.resource.RDFResource]).
     * @return [SolidNetworkResponse.Success] with the resource, or an error/exception variant.
     */
    suspend fun <T: Resource> read(
        webid: String,
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T>

    /**
     * Creates a new resource on the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The resource to create; its identifier determines the target URI.
     * @return [SolidNetworkResponse.Success] with the created resource.
     */
    suspend fun <T: Resource> create(
        webid: String,
        resource: T
    ): SolidNetworkResponse<T>

    /**
     * Replaces an existing resource on the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param newResource The updated resource; its identifier determines the target URI.
     * @return [SolidNetworkResponse.Success] with the updated resource.
     */
    suspend fun <T: Resource> update(
        webid: String,
        newResource: T
    ): SolidNetworkResponse<T>

    /**
     * Deletes a resource from the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The resource to delete.
     * @return [SolidNetworkResponse.Success] with the deleted resource.
     */
    suspend fun <T: Resource> delete(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T>

    /**
     * Recursively deletes a container and all of its contents.
     * @param webid The WebID of the authenticated user making the request.
     * @param containerUri The URI of the LDP container to delete.
     * @return [SolidNetworkResponse.Success] with `true` on success.
     */
    suspend fun deleteContainer(
        webid: String,
        containerUri: URI
    ): SolidNetworkResponse<Boolean>
}
