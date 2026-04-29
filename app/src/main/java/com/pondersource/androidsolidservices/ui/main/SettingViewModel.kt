package com.pondersource.androidsolidservices.ui.main

import android.accounts.Account
import android.accounts.AccountManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.shared.domain.profile.Profile
import com.pondersource.solidandroidapi.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val authenticator: Authenticator,
    @Named(Constants.ASS_ACCOUNT_NAME) private val aSSAccountName: String,
) : BaseViewModel() {

    val accounts: StateFlow<List<Profile>> = authenticator.loggedInProfilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeWebId: StateFlow<String> = authenticator.activeWebIdFlow
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val logoutLoading = mutableStateOf(false)

    val navigateToLogin: StateFlow<Boolean> = authenticator.isAuthorizedFlow
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun switchAccount(webId: String) {
        viewModelScope.launch {
            authenticator.setActiveWebId(webId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutLoading.value = true
            val webId = activeWebId.value
            authenticator.removeProfile(webId)
            accountManager.removeAccountExplicitly(Account(webId, aSSAccountName))
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
            authenticator.removeAllProfiles()
            logoutLoading.value = false
        }
    }
}