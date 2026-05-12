package com.erfangholami.androidsolidservices.client.sdk

public data class SolidSignInAccount(
    val packageName: String,
    val webId: String,
    val fullAccess: Boolean = true
)