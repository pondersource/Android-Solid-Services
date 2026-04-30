package com.pondersource.solidandroidclient.sdk

data class SolidSignInAccount(
    val packageName: String,
    val webId: String,
    val fullAccess: Boolean = true
)