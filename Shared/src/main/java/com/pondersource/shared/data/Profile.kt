package com.pondersource.shared.data

import com.pondersource.shared.data.webid.WebId
import kotlinx.serialization.json.Json
import com.pondersource.shared.data.webid.WebId.Companion.readFromString
import com.pondersource.shared.data.webid.WebId.Companion.writeToString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.openid.appauth.AuthState

@Serializable
data class ProfileList(
    @Serializable(with = ProfileMapSerializer::class)
    val profiles: Map<String, Profile> = mapOf()
)

fun ProfileList.contains(webId: String): Boolean {
    return profiles.containsKey(webId)
}

fun ProfileList.getProfileOrNull(webId: String): Profile? {
    return profiles[webId]
}

@Serializable(with = ProfileSerializer::class)
data class Profile(
    val authState: AuthState = AuthState(),
    val userInfo: UserInfo? = null,
    val webId: WebId? = null
)

@Serializer(forClass = Map::class)
class ProfileMapSerializer(
    private val keySerializer: KSerializer<String>,
    private val valueSerializer: KSerializer<Profile>
) : KSerializer<Map<String, Profile>> {
    override fun serialize(encoder: Encoder, value: Map<String, Profile>) {
        MapSerializer(keySerializer, valueSerializer)
            .serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, Profile> {
        return MapSerializer(keySerializer, valueSerializer)
            .deserialize(decoder)
    }
}

@Serializer(forClass = Profile::class)
class ProfileSerializer: KSerializer<Profile> {
    override fun serialize(encoder: Encoder, value: Profile) {
        encoder.encodeString(value.authState.jsonSerializeString())
        encoder.encodeString(if (value.userInfo != null) Json.encodeToString(value.userInfo) else "")
        encoder.encodeString(writeToString(value.webId) ?: "")
    }

    override fun deserialize(decoder: Decoder): Profile {
        val stateString = decoder.decodeString()
        val userInfoString = decoder.decodeString()
        val webIdString = decoder.decodeString()
        return Profile(
            authState = if (stateString.isNotEmpty()) AuthState.jsonDeserialize(stateString) else AuthState(),
            userInfo = if (userInfoString.isNotEmpty()) Json.decodeFromString<UserInfo>(userInfoString) else null,
            webId = if (webIdString.isNotEmpty()) readFromString(webIdString) else null,
        )
    }
}