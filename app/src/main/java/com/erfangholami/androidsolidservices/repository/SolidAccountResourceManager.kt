package com.erfangholami.androidsolidservices.repository

import com.erfangholami.androidsolidservices.shared.domain.network.SolidNetworkResponse
import com.erfangholami.androidsolidservices.shared.domain.resource.Resource
import java.net.URI

interface SolidAccountResourceManager {

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
}