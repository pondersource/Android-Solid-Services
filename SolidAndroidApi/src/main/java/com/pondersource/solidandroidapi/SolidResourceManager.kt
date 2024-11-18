package com.pondersource.solidandroidapi

import java.net.URI

interface SolidResourceManager {

    suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> read(
        resource: URI,
        clazz: Class<T>,
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T>

    suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> create(
        resource: T
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T>

    suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> update(
        newResource: T
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T>

    suspend fun <T: com.pondersource.solidandroidclient.sub.resource.Resource> delete(
        resource: T,
    ): com.pondersource.solidandroidclient.SolidNetworkResponse<T>
}
