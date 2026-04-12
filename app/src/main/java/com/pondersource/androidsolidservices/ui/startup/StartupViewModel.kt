package com.pondersource.androidsolidservices.ui.startup

import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.solidandroidapi.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    val authenticator: Authenticator,
) : BaseViewModel(){

    suspend fun isLoggedIn(): Boolean {
        // getActiveWebId() awaits Authenticator init, ensuring profiles are loaded
        // before checking authorization state.
        authenticator.getActiveWebId()
        return authenticator.isUserAuthorized()
    }
}