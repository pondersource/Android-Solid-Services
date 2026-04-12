package com.pondersource.androidsolidservices.ui.main

import android.accounts.Account
import android.accounts.AccountManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.base.Constants
import com.pondersource.solidandroidapi.Authenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val authenticator: Authenticator,
    @Named(Constants.ASS_ACCOUNT_NAME) private val aSSAccountName: String,
): BaseViewModel() {

    val activeProfile = mutableStateOf(authenticator.getProfile())
    val webId: MutableState<String> = mutableStateOf(activeProfile.value.userInfo!!.webId)
    val storages = mutableStateOf(activeProfile.value.webId!!.getStorages().map { it.toString() })
    val allProfiles = mutableStateOf(authenticator.getAllLoggedInProfiles())

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