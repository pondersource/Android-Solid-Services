package com.erfangholami.androidsolidservices.ui.startup

import com.erfangholami.androidsolidservices.base.BaseViewModel
import com.erfangholami.androidsolidservices.api.auth.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    val authenticator: Authenticator,
) : BaseViewModel() {

    fun isLoggedIn(): Boolean {
        return authenticator.isUserAuthorized()
    }
}