package com.pondersource.androidsolidservices.ui.main

import androidx.lifecycle.viewModelScope
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.model.GrantedApp
import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccessGrantViewModel @Inject constructor(
    private val accessGrantRepository: AccessGrantRepository,
) : BaseViewModel() {

    val grantedApps = accessGrantRepository.grantedApplications()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun revokeAccess(app: GrantedApp) {
        viewModelScope.launch {
            accessGrantRepository.revokeAccessGrant(app.packageName, app.webId)
        }
    }
}