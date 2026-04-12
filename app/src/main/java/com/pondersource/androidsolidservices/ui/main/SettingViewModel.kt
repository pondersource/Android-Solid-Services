package com.pondersource.androidsolidservices.ui.main

import android.accounts.Account
import android.accounts.AccountManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.shared.data.Profile
import com.pondersource.solidandroidapi.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val authenticator: Authenticator,
    @Named(Constants.ASS_ACCOUNT_NAME) private val aSSAccountName: String,
): BaseViewModel() {

    val accounts = mutableStateOf<List<Profile>>(authenticator.getAllLoggedInProfiles())
    val activeWebId = mutableStateOf(runBlocking { authenticator.getActiveWebId() ?: "" })
    val logoutLoading = mutableStateOf(false)
    /** True when there are no accounts left — navigate to Login. */
    val navigateToLogin = mutableStateOf(false)

    fun switchAccount(webId: String) {
        viewModelScope.launch {
            authenticator.setActiveWebId(webId)
            activeWebId.value = webId
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutLoading.value = true
            val webId = activeWebId.value
            authenticator.resetProfile(webId)
            accountManager.removeAccountExplicitly(Account(webId, aSSAccountName))
            // Refresh reactive state
            accounts.value = authenticator.getAllLoggedInProfiles()
            val remaining = accounts.value
            if (remaining.isNotEmpty()) {
                activeWebId.value = authenticator.getActiveWebId() ?: ""
            } else {
                navigateToLogin.value = true
            }
            logoutLoading.value = false
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            logoutLoading.value = true
            authenticator.getAllLoggedInProfiles().forEach { profile ->
                accountManager.removeAccountExplicitly(
                    Account(profile.userInfo!!.webId, aSSAccountName)
                )
            }
            authenticator.resetProfile()
            accounts.value = emptyList()
            logoutLoading.value = false
            navigateToLogin.value = true
        }
    }

    fun refreshAccounts() {
        accounts.value = authenticator.getAllLoggedInProfiles()
        viewModelScope.launch {
            activeWebId.value = authenticator.getActiveWebId() ?: ""
        }
    }
}