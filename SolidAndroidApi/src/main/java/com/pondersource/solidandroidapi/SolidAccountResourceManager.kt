package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.profile.Profile
import com.pondersource.shared.domain.resource.Resource
import java.net.URI

interface SolidAccountResourceManager {

    companion object {
        fun getInstance(context: Context, profile: Profile): SolidAccountResourceManager =
            SolidAccountResourceManagerImplementation(context, profile)
    }

    suspend fun <T : Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T>

    suspend fun <T : Resource> create(
        resource: T
    ): SolidNetworkResponse<T>

    suspend fun <T : Resource> update(
        newResource: T
    ): SolidNetworkResponse<T>

    suspend fun <T : Resource> delete(
        resource: T,
    ): SolidNetworkResponse<T>

    suspend fun delete(
        resourceUri: URI,
    ): SolidNetworkResponse<Boolean>

    suspend fun deleteContainer(
        containerUri: URI
    ): SolidNetworkResponse<Boolean>
}