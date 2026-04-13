package com.pondersource.solidandroidapi.repository

import android.content.Context
import com.pondersource.shared.data.Profile
import com.pondersource.shared.data.ProfileList
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    companion object {
        fun getInstance(context: Context): UserRepository = UserRepositoryImplementation.getInstance(context)
    }

    fun readAllProfiles(): Flow<ProfileList>
    fun activeWebIdFlow(): Flow<String?>
    suspend fun writeProfile(webid: String, profile: Profile)
    suspend fun removeProfile(webid: String)
    suspend fun removeAllProfiles()
    suspend fun getActiveWebId(): String?
    suspend fun setActiveWebId(webId: String?)
}