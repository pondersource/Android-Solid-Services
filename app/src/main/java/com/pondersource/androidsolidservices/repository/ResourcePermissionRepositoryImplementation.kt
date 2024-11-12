package com.pondersource.androidsolidservices.repository

import com.pondersource.androidsolidservices.model.PermissionType

class ResourcePermissionRepositoryImplementation(
    private val accessGrantRepository: AccessGrantRepository,
): ResourcePermissionRepository {


    override fun hasAccess(
        resourceClaimantPackageName: String,
        resourceUrl: String,
        permissionType: PermissionType
    ): Boolean {
        return accessGrantRepository.hasAccessGrant(resourceClaimantPackageName)
    }
}