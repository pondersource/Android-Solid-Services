package com.pondersource.androidsolidservices.model

import kotlinx.serialization.Serializable

@Serializable
data class GrantedApp(
    val packageName: String,
    val name: String,
)
