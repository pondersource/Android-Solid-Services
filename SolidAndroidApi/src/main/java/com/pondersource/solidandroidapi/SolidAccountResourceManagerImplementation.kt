package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.profile.Profile
import com.pondersource.shared.domain.resource.Resource
import java.net.URI
import java.security.InvalidParameterException

internal class SolidAccountResourceManagerImplementation(
    context: Context,
    private val profile: Profile,
) : SolidAccountResourceManager {

    private val resourceManager = SolidResourceManagerImplementation.getInstance(context)

    init {
        if (profile.userInfo?.webId.isNullOrEmpty()) {
            throw InvalidParameterException("WebId is null or empty.")
        }
    }

    override suspend fun <T : Resource> read(
        resource: URI,
        clazz: Class<T>
    ): SolidNetworkResponse<T> {
        return resourceManager.read(profile.userInfo!!.webId, resource, clazz)
    }

    override suspend fun <T : Resource> create(resource: T): SolidNetworkResponse<T> {
        return resourceManager.create(profile.userInfo!!.webId, resource)
    }

    override suspend fun <T : Resource> update(newResource: T): SolidNetworkResponse<T> {
        return resourceManager.update(profile.userInfo!!.webId, newResource)
    }

    override suspend fun <T : Resource> delete(resource: T): SolidNetworkResponse<T> {
        return resourceManager.delete(profile.userInfo!!.webId, resource)
    }

    override suspend fun deleteContainer(containerUri: URI): SolidNetworkResponse<Boolean> {
        return resourceManager.deleteContainer(profile.userInfo!!.webId, containerUri)
    }
}