package com.pondersource.solidandroidapi.repository

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.pondersource.shared.data.Profile
import com.pondersource.shared.data.ProfileList
import com.pondersource.shared.data.contains
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserRepositoryImplementation private constructor(
    private val context: Context,
): UserRepository {

    companion object {

        private const val PROFILES_FILE_NAME = "profiles.json"

        @Volatile
        private lateinit var INSTANCE: UserRepository

        object ProfileListSerializer: Serializer<ProfileList> {
            override val defaultValue: ProfileList
                get() = ProfileList()

            override suspend fun readFrom(input: InputStream): ProfileList {
                try {
                    return Json.decodeFromString<ProfileList>(
                        input.readBytes().decodeToString()
                    )
                } catch (serialization: SerializationException) {
                    throw CorruptionException("Unable to read Settings", serialization)
                }
            }

            override suspend fun writeTo(
                t: ProfileList,
                output: OutputStream
            ) {
                output.write(
                    Json.encodeToString(t)
                        .encodeToByteArray()
                )
            }
        }

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

    private val Context.profilesDataStore: DataStore<ProfileList> by dataStore(
        fileName = PROFILES_FILE_NAME,
        serializer = ProfileListSerializer,
    )

    override fun readAllProfiles(): Flow<ProfileList> {
        return context.profilesDataStore.data
    }

    override suspend fun writeProfile(webid: String, profile: Profile) {
        context.profilesDataStore.updateData {
            it.copy(profiles = it.profiles.toMutableMap().apply {
                put(webid, profile)
            })
        }
    }

    override suspend fun removeProfile(webid: String) {
        context.profilesDataStore.updateData {
            if(it.contains(webid)) {
                it.copy(profiles = it.profiles.toMutableMap().apply {
                    remove(webid)
                })
            } else {
                it
            }
        }
    }
}