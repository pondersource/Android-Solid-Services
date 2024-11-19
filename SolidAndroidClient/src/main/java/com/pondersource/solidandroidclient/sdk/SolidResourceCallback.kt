package com.pondersource.solidandroidclient.sdk

import com.pondersource.shared.resource.Resource
import com.pondersource.solidandroidclient.sdk.SolidException.SolidResourceException

interface SolidResourceCallback <T: Resource> {

    fun onResult(result: T)
    fun onError(exception: SolidResourceException)
}