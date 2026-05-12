package com.erfangholami.androidsolidservices.repository

import com.erfangholami.androidsolidservices.model.GrantedApp
import com.erfangholami.androidsolidservices.repository.datasource.local.accessgrant.AccessGrantLocalDataSource
import kotlinx.coroutines.flow.Flow

class AccessGrantRepositoryImplementation(
    private val accessGrantLocalDataSource: AccessGrantLocalDataSource,
) : AccessGrantRepository {

    override fun hasAccessGrant(appPackageName: String, webId: String): Boolean {
        return accessGrantLocalDataSource.hasAccessGrant(appPackageName, webId)
    }

    override suspend fun addAccessGrant(
        appPackageName: String,
        appName: String,
        webId: String,
    ) {
        accessGrantLocalDataSource.addAccessGrant(appPackageName, appName, webId)
    }

    override suspend fun revokeAccessGrant(appPackageName: String, webId: String) {
        accessGrantLocalDataSource.revokeAccessGrant(appPackageName, webId)
    }

    override fun grantedApplications(): Flow<List<GrantedApp>> {
        return accessGrantLocalDataSource.grantedApplications()
    }
}