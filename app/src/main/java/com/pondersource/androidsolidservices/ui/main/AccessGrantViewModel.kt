package com.pondersource.androidsolidservices.ui.main

import androidx.compose.runtime.mutableStateOf
import com.pondersource.androidsolidservices.base.BaseViewModel
import com.pondersource.androidsolidservices.model.GrantedApp
import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccessGrantViewModel @Inject constructor(
    private val accessGrantRepository: AccessGrantRepository,
): BaseViewModel() {

    val grantedApps = mutableStateOf(accessGrantRepository.grantedApplications())

    fun revokeAccess(app: GrantedApp) {
        accessGrantRepository.revokeAccessGrant(app.packageName)
        grantedApps.value = accessGrantRepository.grantedApplications()
    }
}