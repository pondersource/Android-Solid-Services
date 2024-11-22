package com.pondersource.shared.data

import net.openid.appauth.AuthState

data class Profile(
    var authState: AuthState = AuthState(),
    var userInfo: UserInfo? = null,
    var webId: WebId? = null
)