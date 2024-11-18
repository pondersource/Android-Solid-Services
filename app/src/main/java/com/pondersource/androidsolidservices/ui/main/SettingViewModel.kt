package com.pondersource.androidsolidservices.ui.main

import android.accounts.Account
import android.accounts.AccountManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.solidandroidapi.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val authenticator: Authenticator,
    @Named(Constants.ASS_ACCOUNT_NAME) private val aSSAccountName: String,
): BaseViewModel() {

    companion object {
        private const val AUTH_END_REDIRECT_URL = "com.pondersource.androidsolidservices:/oauth2logout"
    }

    private lateinit var account: Account
    val logoutLoading = mutableStateOf(false)
    val logoutResult = mutableStateOf(false)

    init {
        handleAccountManagement()
    }

    fun logout() {
        logoutLoading.value = true
        authenticator.resetProfile()
        val deleteRes = accountManager.removeAccountExplicitly(account)
        logoutLoading.value = false
        logoutResult.value = true
    }

    fun logoutWithBrowser() {
        viewModelScope.launch {
            authenticator.getTerminationSessionIntent(
                AUTH_END_REDIRECT_URL
            )
        }
    }

    private fun handleAccountManagement() {
        account = Account(
            authenticator.getProfile().userInfo!!.webId,
            aSSAccountName
        )

        var hasAddedBefore = false
        accountManager.accounts.forEach { acc ->
            if (acc.type == account.type && acc.name == account.name) {
                hasAddedBefore = true
            }
        }

        if (!hasAddedBefore) {
            accountManager.addAccountExplicitly(account, "password", null)
        }

    }
}