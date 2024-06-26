package com.pondersource.solidandroidclient.data

import com.google.gson.Gson
import com.pondersource.solidandroidclient.WebId
import net.openid.appauth.AuthState

data class Profile(
    var authState: AuthState = AuthState(),
    var userInfo: UserInfo? = null,
    var webIdDetails: WebId? = null
) {
    companion object {
        fun toGsonString(profile: Profile): String {
            return Gson().toJson(profile)
        }

        fun fromGsonString(gsonString: String?): Profile {
            if (gsonString.isNullOrEmpty()) {
                return Profile()
            }
            return Gson().fromJson(gsonString, Profile::class.java)
        }
    }
}
