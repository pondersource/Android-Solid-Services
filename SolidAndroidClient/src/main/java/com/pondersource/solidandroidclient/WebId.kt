package com.pondersource.solidandroidclient

import com.google.gson.annotations.SerializedName

class WebId(
    @SerializedName("@id")
    val id: String,
    @SerializedName("@type")
    val type: String,
    @SerializedName("http://xmlns.com/foaf/0.1/isPrimaryTopicOf")
    val isPrimaryTypeOf: JObject,
    @SerializedName("http://www.w3.org/ns/solid/terms#oidcIssuer")
    val oidcIssuer: JObject,
    @SerializedName("http://www.w3.org/ns/pim/space#storage")
    val storage: JObject,
    @SerializedName("http://www.w3.org/2000/01/rdf-schema#seeAlso")
    val seeAlso: JObject,
)

data class JObject(
    @SerializedName("@id")
    val id: String
)