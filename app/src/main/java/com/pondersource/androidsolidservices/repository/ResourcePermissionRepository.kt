package com.pondersource.androidsolidservices.repository

import com.pondersource.androidsolidservices.model.PermissionType

interface ResourcePermissionRepository {

    fun hasAccess(
        webId: String,
        resourceClaimantPackageName: String,
        resourceUrl: String,
        permissionType: PermissionType
    ): Boolean
}