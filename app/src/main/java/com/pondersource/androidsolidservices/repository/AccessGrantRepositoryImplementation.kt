package com.pondersource.androidsolidservices.repository

import com.pondersource.androidsolidservices.model.GrantedApp
import com.pondersource.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSource

class AccessGrantRepositoryImplementation(
    private val accessGrantLocalDataSource: AccessGrantLocalDataSource,
): AccessGrantRepository {

    override fun hasAccessGrant(appPackageName: String): Boolean {
        return accessGrantLocalDataSource.hasAccessGrant(appPackageName)
    }

    override fun addAccessGrant(
        appPackageName: String,
        appName: String,
    ) {
        accessGrantLocalDataSource.addAccessGrant(appPackageName, appName)
    }

    override fun revokeAccessGrant(appPackageName: String) {
        accessGrantLocalDataSource.revokeAccessGrant(appPackageName)
    }

    override fun grantedApplications(): List<GrantedApp> {
        return accessGrantLocalDataSource.grantedApplications()
    }
}