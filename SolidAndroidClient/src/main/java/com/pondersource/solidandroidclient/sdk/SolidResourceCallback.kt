package com.pondersource.solidandroidclient.sdk

import com.pondersource.solidandroidclient.sdk.SolidException.SolidResourceException
import com.pondersource.solidandroidclient.sub.resource.Resource

interface SolidResourceCallback <T: Resource> {

    fun onResult(result: T)
    fun onError(exception: SolidResourceException)
}