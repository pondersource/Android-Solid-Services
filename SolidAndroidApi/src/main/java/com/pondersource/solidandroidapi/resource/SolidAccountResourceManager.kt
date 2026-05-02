package com.pondersource.solidandroidapi.resource

import android.content.Context
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.profile.Profile
import com.pondersource.shared.domain.resource.Resource
import com.pondersource.solidandroidapi.resource.implementation.SolidAccountResourceManagerImplementation
import java.net.URI

/**
 * Profile-scoped variant of [SolidResourceManager] that binds all operations to a fixed [Profile].
 *
 * Callers do not supply a `webid` on each call — it is derived from the profile provided at
 * construction time. Use this when all operations in a scope belong to the same user.
 *
 * Obtain an instance via [SolidAccountResourceManager.getInstance].
 */
public interface SolidAccountResourceManager {

    public companion object {
        /**
         * Returns a [SolidAccountResourceManager] bound to [profile].
         * @param context Any [Context]; the application context is used internally.
         * @param profile The profile whose WebID will be used for all operations.
         */
        public fun getInstance(context: Context, profile: Profile): SolidAccountResourceManager =
            SolidAccountResourceManagerImplementation(context, profile)
    }

    /**
     * Reads a resource from the pod.
     * @param resource The URI of the resource to read.
     * @param clazz The expected resource type.
     * @return [SolidNetworkResponse.Success] with the resource, or an error/exception variant.
     */
    public suspend fun <T : Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T>

    /**
     * Creates a new resource on the pod.
     * @param resource The resource to create; its identifier determines the target URI.
     * @return [SolidNetworkResponse.Success] with the created resource.
     */
    public suspend fun <T : Resource> create(
        resource: T
    ): SolidNetworkResponse<T>

    /**
     * Replaces an existing resource on the pod via HTTP PUT.
     * @param newResource The updated resource; its identifier determines the target URI.
     * @return [SolidNetworkResponse.Success] with the updated resource.
     */
    public suspend fun <T : Resource> update(
        newResource: T
    ): SolidNetworkResponse<T>

    /**
     * Deletes a resource from the pod.
     * @param resource The resource to delete.
     * @return [SolidNetworkResponse.Success] with the deleted resource.
     */
    public suspend fun <T : Resource> delete(
        resource: T,
    ): SolidNetworkResponse<T>

    /**
     * Deletes a resource from the pod by URI.
     * @param resourceUri The URI of the resource to delete.
     * @return [SolidNetworkResponse.Success] with `true` on success.
     */
    public suspend fun delete(
        resourceUri: URI,
    ): SolidNetworkResponse<Boolean>

}
