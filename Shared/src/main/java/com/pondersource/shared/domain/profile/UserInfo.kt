package com.pondersource.shared.domain.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("webid")
    val webId: String,
)