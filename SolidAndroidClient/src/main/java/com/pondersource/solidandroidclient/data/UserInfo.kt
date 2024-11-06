package com.pondersource.solidandroidclient.data

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("webid")
    val webId: String,
)