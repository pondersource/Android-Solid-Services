package com.pondersource.shared.domain.profile

import com.pondersource.shared.domain.profile.WebId.Companion.readFromString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
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

@OptIn(ExperimentalSerializationApi::class)
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

class ProfileSerializer : KSerializer<Profile> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Profile") {
        element<String>("authState")
        element<String>("userInfo")
        element<String>("webId")
    }

    override fun serialize(encoder: Encoder, value: Profile) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.authState.jsonSerializeString())
            encodeStringElement(
                descriptor,
                1,
                if (value.userInfo != null) Json.encodeToString(value.userInfo) else ""
            )
            encodeStringElement(descriptor, 2, WebId.Companion.writeToString(value.webId) ?: "")
        }
    }

    override fun deserialize(decoder: Decoder): Profile {
        var stateString = ""
        var userInfoString = ""
        var webIdString = ""

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> stateString = decodeStringElement(descriptor, 0)
                    1 -> userInfoString = decodeStringElement(descriptor, 1)
                    2 -> webIdString = decodeStringElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return Profile(
            authState = if (stateString.isNotEmpty()) AuthState.jsonDeserialize(stateString) else AuthState(),
            userInfo = if (userInfoString.isNotEmpty()) Json.decodeFromString<UserInfo>(
                userInfoString
            ) else null,
            webId = if (webIdString.isNotEmpty()) readFromString(webIdString) else null,
        )
    }
}