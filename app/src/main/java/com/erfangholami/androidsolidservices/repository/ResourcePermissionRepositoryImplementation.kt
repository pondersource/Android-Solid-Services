package com.erfangholami.androidsolidservices.repository

import com.erfangholami.androidsolidservices.model.PermissionType

class ResourcePermissionRepositoryImplementation(
    private val accessGrantRepository: AccessGrantRepository,
) : ResourcePermissionRepository {


    override fun hasAccess(
        webId: String,
        resourceClaimantPackageName: String,
        resourceUrl: String,
        permissionType: PermissionType
    ): Boolean {
        return accessGrantRepository.hasAccessGrant(resourceClaimantPackageName, webId)
    }
}