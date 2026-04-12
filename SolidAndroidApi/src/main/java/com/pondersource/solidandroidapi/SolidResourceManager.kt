package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.SolidNetworkResponse
import com.pondersource.shared.resource.Resource
import java.net.URI

interface SolidResourceManager {

    companion object {
        fun getInstance(context: Context): SolidResourceManager = SolidResourceManagerImplementation.getInstance(context)
    }

    suspend fun <T: Resource> read(
        webid: String,
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T>

    suspend fun <T: Resource> create(
        webid: String,
        resource: T
    ): SolidNetworkResponse<T>

    suspend fun <T: Resource> update(
        webid: String,
        newResource: T
    ): SolidNetworkResponse<T>

    suspend fun <T: Resource> delete(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T>

    suspend fun deleteContainer(
        webid: String,
        containerUri: URI
    ): SolidNetworkResponse<Boolean>
}
