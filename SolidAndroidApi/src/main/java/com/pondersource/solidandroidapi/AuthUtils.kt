package com.pondersource.solidandroidapi

import net.openid.appauth.AuthorizationServiceDiscovery
import org.json.JSONArray
import org.json.JSONException

private const val DPOP_SIGNING_ALG_VALUES_SUPPORTED = "dpop_signing_alg_values_supported"

fun AuthorizationServiceDiscovery.supportsDPop(): Boolean {
    return this.docJson.has(DPOP_SIGNING_ALG_VALUES_SUPPORTED)
}

fun AuthorizationServiceDiscovery.supportedDPopAlgorithms(): List<String> {
    return if(this.supportsDPop()) {
        this.get(DPOP_SIGNING_ALG_VALUES_SUPPORTED)!!
    } else {
        listOf()
    }
}

private fun AuthorizationServiceDiscovery.get(field: String): List<String>? {
    try {
        if (!this.docJson.has(field)) {
            return null
        }
        val value = this.docJson.get(field)
        check(value is JSONArray) {
            (field
                    + " does not contain the expected JSON array")
        }
        val values = ArrayList<String>()
        for (i in 0..<value.length()) {
            values.add(value.getString(i))
        }
        return values
    } catch (e: JSONException) {
        // all appropriate steps are taken above to avoid a JSONException. If it is still
        // thrown, indicating an implementation change, throw an excpetion
        throw IllegalStateException("unexpected JSONException", e)
    }
}

