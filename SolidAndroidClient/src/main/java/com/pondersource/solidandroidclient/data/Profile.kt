package com.pondersource.solidandroidclient.data

import net.openid.appauth.AuthState

data class Profile(
    var authState: AuthState = AuthState(),
    var userInfo: UserInfo? = null,
    var webId: WebIdProfile? = null
)