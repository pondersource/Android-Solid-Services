package com.pondersource.androidsolidservices.repository

import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.Resource
import java.net.URI

interface SolidAccountResourceManager {

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