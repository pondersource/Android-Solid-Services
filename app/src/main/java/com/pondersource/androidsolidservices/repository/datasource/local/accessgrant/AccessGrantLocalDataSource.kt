package com.pondersource.androidsolidservices.repository.datasource.local.accessgrant

import com.pondersource.androidsolidservices.model.GrantedApp

interface AccessGrantLocalDataSource {

    fun hasAccessGrant(appPackageName: String): Boolean

    fun addAccessGrant(appPackageName: String, appName: String, appIcon: Int)

    fun revokeAccessGrant(appPackageName: String)

    fun grantedApplications(): List<GrantedApp>
}