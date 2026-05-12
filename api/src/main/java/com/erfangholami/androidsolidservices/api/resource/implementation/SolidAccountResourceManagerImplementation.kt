package com.erfangholami.androidsolidservices.api.resource.implementation

import com.erfangholami.androidsolidservices.shared.domain.network.SolidNetworkResponse
import com.erfangholami.androidsolidservices.shared.domain.profile.Profile
import com.erfangholami.androidsolidservices.shared.domain.resource.Resource
import com.erfangholami.androidsolidservices.api.auth.Authenticator
import com.erfangholami.androidsolidservices.api.resource.SolidAccountResourceManager
import com.erfangholami.androidsolidservices.api.resource.SolidResourceManager
import java.net.URI
import java.security.InvalidParameterException

internal class SolidAccountResourceManagerImplementation(
    authenticator: Authenticator,
    private val profile: Profile,
) : SolidAccountResourceManager {

    private val resourceManager = SolidResourceManager.getInstance(authenticator)

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

    override suspend fun delete(resourceUri: URI): SolidNetworkResponse<Boolean> {
        return resourceManager.delete(profile.userInfo!!.webId, resourceUri)
    }

}