package com.pondersource.solidandroidclient

import com.google.gson.annotations.SerializedName

class WebId(
    @SerializedName("@id")
    val id: String? = null,
    @SerializedName("@type")
    val type: String? = null,
    @SerializedName("http://xmlns.com/foaf/0.1/isPrimaryTopicOf")
    val isPrimaryTypeOf: JObject? = null,
    @SerializedName("http://www.w3.org/ns/solid/terms#oidcIssuer")
    val oidcIssuer: JObject? = null,
    @SerializedName("http://www.w3.org/ns/pim/space#storage")
    var storage: JObject? = null,
    @SerializedName("http://www.w3.org/2000/01/rdf-schema#seeAlso")
    val seeAlso: JObject? = null,
)

data class JObject(
    @SerializedName("@id")
    val id: String?
)