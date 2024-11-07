package com.pondersource.androidsolidservices.ui.startup

import com.pondersource.androidsolidservices.usecase.Authenticator
import com.pondersource.androidsolidservices.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    val authenticator: Authenticator,
) : BaseViewModel(){

    suspend fun isLoggedIn(): Boolean {
        return if(authenticator.isUserAuthorized()) {
            authenticator.getLastTokenResponse()
            true
        } else {
            false
        }
    }
}