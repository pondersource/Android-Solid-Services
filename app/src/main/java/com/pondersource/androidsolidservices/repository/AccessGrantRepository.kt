package com.pondersource.androidsolidservices.repository

import com.pondersource.androidsolidservices.model.GrantedApp

interface AccessGrantRepository {

    fun hasAccessGrant(appPackageName: String): Boolean

    fun addAccessGrant(appPackageName: String, appName: String)

    fun revokeAccessGrant(appPackageName: String)

    fun grantedApplications(): List<GrantedApp>
}