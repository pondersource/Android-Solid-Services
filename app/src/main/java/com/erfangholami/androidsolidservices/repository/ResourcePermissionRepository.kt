package com.erfangholami.androidsolidservices.repository

import com.erfangholami.androidsolidservices.model.PermissionType

interface ResourcePermissionRepository {

    fun hasAccess(
        webId: String,
        resourceClaimantPackageName: String,
        resourceUrl: String,
        permissionType: PermissionType
    ): Boolean
}