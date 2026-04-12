package com.pondersource.solidandroidapi.repository

import com.pondersource.shared.data.Profile
import com.pondersource.shared.data.ProfileList
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun readAllProfiles(): Flow<ProfileList>

    suspend fun writeProfile(webid: String, profile: Profile)

    suspend fun removeProfile(webid: String)
}