package com.pondersource.androidsolidservices.services

import com.pondersource.shared.domain.auth.IASSLoginCallback
import java.util.concurrent.ConcurrentHashMap

data class PendingLoginRequest(
    val callerPackage: String,
    val callerName: String,
    val callback: IASSLoginCallback,
)

object PendingLoginRequests {
    private val map = ConcurrentHashMap<String, PendingLoginRequest>()

    fun put(id: String, request: PendingLoginRequest) {
        map[id] = request
    }

    fun get(id: String): PendingLoginRequest? = map[id]

    fun remove(id: String): PendingLoginRequest? = map.remove(id)
}
