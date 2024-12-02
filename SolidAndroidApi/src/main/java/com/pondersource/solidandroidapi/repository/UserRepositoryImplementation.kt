package com.pondersource.solidandroidapi.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.pondersource.shared.data.Profile
import com.pondersource.shared.data.UserInfo
import com.pondersource.shared.data.webid.WebId.Companion.readFromString
import com.pondersource.shared.data.webid.WebId.Companion.writeToString
import net.openid.appauth.AuthState

class UserRepositoryImplementation: UserRepository {

    companion object {

        private const val SHARED_PREFS_NAME = "solid_android_api_shared_prefs"
        private const val PROFILE_STATE_KEY = "profile_state"
        private const val PROFILE_USER_INFO_KEY = "profile_user_info"
        private const val PROFILE_WEB_ID_DETAILS_KEY = "profile_web_id_details"

        @Volatile
        private lateinit var INSTANCE: UserRepository

        fun getInstance(
            context: Context,
        ): UserRepository {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = UserRepositoryImplementation(context)
                INSTANCE
            }
        }
    }

    private val sharedPreferences: SharedPreferences

    private constructor(context: Context) {
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
    }

    override fun readProfile(): Profile {
        val profile = Profile()
        val stateString = sharedPreferences.getString(PROFILE_STATE_KEY, null)
        val webIdString = sharedPreferences.getString(PROFILE_WEB_ID_DETAILS_KEY, null)
        if (!stateString.isNullOrEmpty()) {
            profile.authState =  AuthState.jsonDeserialize(stateString)
        }
        profile.userInfo = Gson().fromJson(sharedPreferences.getString(PROFILE_USER_INFO_KEY, null), UserInfo::class.java)
        if (!webIdString.isNullOrEmpty()) {
            profile.webId = readFromString(webIdString)
        }
        return profile
    }

    override fun writeProfile(profile: Profile) {
        sharedPreferences.edit().apply {
            putString(PROFILE_STATE_KEY, profile.authState.jsonSerializeString())
            putString(PROFILE_USER_INFO_KEY, Gson().toJson(profile.userInfo))
            putString(PROFILE_WEB_ID_DETAILS_KEY, writeToString(profile.webId))
            apply()
        }
    }

}