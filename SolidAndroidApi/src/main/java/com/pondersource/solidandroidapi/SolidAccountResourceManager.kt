package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.SolidNetworkResponse
import com.pondersource.shared.data.Profile
import com.pondersource.shared.resource.Resource
import java.net.URI

interface SolidAccountResourceManager {

    companion object {
        fun getInstance(context: Context, profile: Profile): SolidAccountResourceManager =
            SolidAccountResourceManagerImplementation(context, profile)
    }

    suspend fun <T: Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T>

    suspend fun <T: Resource> create(
        resource: T
    ): SolidNetworkResponse<T>

    suspend fun <T: Resource> update(
        newResource: T
    ): SolidNetworkResponse<T>

    suspend fun <T: Resource> delete(
        resource: T,
    ): SolidNetworkResponse<T>

    suspend fun deleteContainer(
        containerUri: URI
    ): SolidNetworkResponse<Boolean>
}