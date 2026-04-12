package com.pondersource.androidsolidservices.ui.main

import android.accounts.Account
import android.accounts.AccountManager
import androidx.lifecycle.viewModelScope
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.solidandroidapi.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val authenticator: Authenticator,
    @Named(Constants.ASS_ACCOUNT_NAME) private val aSSAccountName: String,
): BaseViewModel() {

    val webId: StateFlow<String> = authenticator.activeProfileFlow
        .filter { it.userInfo != null }
        .map { it.userInfo!!.webId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val storages: StateFlow<List<String>> = authenticator.activeProfileFlow
        .filter { it.webId != null }
        .map { it.webId!!.getStorages().map { uri -> uri.toString() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        syncAccountManager()
    }

    private fun syncAccountManager() {
        // Ensure all logged-in profiles have an Android Account entry
        val existingAccounts = accountManager.accounts
            .filter { it.type == aSSAccountName }
            .map { it.name }
            .toSet()

        authenticator.getAllLoggedInProfiles().forEach { profile ->
            val profileWebId = profile.userInfo!!.webId
            if (profileWebId !in existingAccounts) {
                accountManager.addAccountExplicitly(
                    Account(profileWebId, aSSAccountName),
                    "password",
                    null
                )
            }
        }
    }
}