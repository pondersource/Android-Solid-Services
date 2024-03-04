package com.pondersource.solidandroidclient.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("sub")
    val sub: String,
    @SerializedName("webid")
    val webId: String,
)

fun userInfoToJsonString(userInfo: UserInfo?): String? {
    return Gson().toJson(userInfo)
}

fun fromJsonStringToUserInfo(string: String?): UserInfo? {
    return Gson().fromJson(string, UserInfo::class.java)
}