package com.pondersource.solidandroidclient.sdk

data class SolidSignInAccount(
    val packageName: String,
    //For now all apps that user allows them to use Solid, have full access to solid pod.
    val fullAccess : Boolean = true
)