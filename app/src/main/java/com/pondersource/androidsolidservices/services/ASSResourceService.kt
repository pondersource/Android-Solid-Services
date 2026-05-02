package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pondersource.androidsolidservices.model.PermissionType
import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import com.pondersource.androidsolidservices.repository.ResourcePermissionRepository
import com.pondersource.shared.domain.resource.SolidContainer
import com.pondersource.shared.domain.error.ExceptionsErrorCode.NOT_PERMISSION
import com.pondersource.shared.domain.error.ExceptionsErrorCode.NULL_WEBID
import com.pondersource.shared.domain.error.ExceptionsErrorCode.SOLID_NOT_LOGGED_IN
import com.pondersource.shared.domain.error.ExceptionsErrorCode.UNKNOWN
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.SolidNonRDFResource
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.solidandroidapi.auth.Authenticator
import com.pondersource.solidandroidapi.resource.SolidResourceManager
import com.pondersource.shared.IASSResourceService
import com.pondersource.shared.domain.IASSUnitCallback
import com.pondersource.shared.domain.resource.IASSContainerCallback
import com.pondersource.shared.domain.resource.IASSSolidMetadataCallback
import com.pondersource.shared.domain.resource.IASSSolidNonRdfResourceCallback
import com.pondersource.shared.domain.resource.IASSSolidRdfResourceCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@AndroidEntryPoint
class ASSResourceService : LifecycleService() {

    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var solidResourceManager: SolidResourceManager

    @Inject
    lateinit var resourcePermissionRepository: ResourcePermissionRepository

    @Inject
    lateinit var accessGrantRepository: AccessGrantRepository

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private val binder = object : IASSResourceService.Stub() {

        private fun handleBasicExceptions(
            webId: String,
            resourceUrl: String,
            callerPackage: String,
            permissionType: PermissionType,
            errorCallback: (Int, String) -> Unit,
            enterFunction: () -> Unit,
        ) {
            if (!authenticator.isUserAuthorized()) {
                errorCallback(SOLID_NOT_LOGGED_IN, "Solid app has not logged in.")
                return
            }
            if (!accessGrantRepository.hasAccessGrant(callerPackage, webId)) {
                errorCallback(NOT_PERMISSION, "App is not authorized for this account.")
                return
            }
            if (!resourcePermissionRepository.hasAccess(webId, callerPackage, resourceUrl, permissionType)) {
                errorCallback(NOT_PERMISSION, "App does not have permission to access the resource.")
                return
            }
            enterFunction()
        }

        override fun getWebId(webId: String, callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                webId,
                "",
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                val profile = authenticator.getProfile(webId)
                val profileWebId = profile.webId
                if (profileWebId != null) {
                    callback.onResult(profileWebId)
                } else {
                    callback.onError(NULL_WEBID, "WebID is null.")
                }
            }
        }

        override fun head(webId: String, resourceUrl: String, callback: IASSSolidMetadataCallback) {
            handleBasicExceptions(
                webId,
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.head(webId, URI.create(resourceUrl))) {
                        is SolidNetworkResponse.Success -> callback.onResult(result.data)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun readContainer(webId: String, containerUrl: String, callback: IASSContainerCallback) {
            handleBasicExceptions(
                webId,
                containerUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.read(
                        webId,
                        URI.create(containerUrl),
                        SolidContainer::class.java,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult(result.data)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun create(
            webId: String,
            resource: SolidNonRDFResource,
            callback: IASSSolidNonRdfResourceCallback
        ) {
            handleBasicExceptions(
                webId,
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.CREATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.create(webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun createRdf(webId: String, resource: SolidRDFResource, callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                webId,
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.CREATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.create(webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun read(webId: String, resourceUrl: String, callback: IASSSolidNonRdfResourceCallback) {
            handleBasicExceptions(
                webId,
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.read(
                        webId,
                        URI.create(resourceUrl),
                        SolidNonRDFResource::class.java,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult(result.data)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun readRdf(webId: String, resourceUrl: String, callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                webId,
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.read(
                        webId,
                        URI.create(resourceUrl),
                        SolidRDFResource::class.java,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult(result.data)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun update(
            webId: String,
            resource: SolidNonRDFResource,
            ifMatch: String?,
            callback: IASSSolidNonRdfResourceCallback,
        ) {
            handleBasicExceptions(
                webId,
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.update(webId, resource, ifMatch)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun updateRdf(
            webId: String,
            resource: SolidRDFResource,
            ifMatch: String?,
            callback: IASSSolidRdfResourceCallback,
        ) {
            handleBasicExceptions(
                webId,
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.update(webId, resource, ifMatch)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun patch(webId: String, resourceUrl: String, patchBody: String, callback: IASSUnitCallback) {
            handleBasicExceptions(
                webId,
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.patchRaw(webId, URI.create(resourceUrl), patchBody)) {
                        is SolidNetworkResponse.Success -> callback.onResult()
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun delete(
            webId: String,
            resource: SolidNonRDFResource,
            callback: IASSSolidNonRdfResourceCallback
        ) {
            handleBasicExceptions(
                webId,
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.delete(webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun deleteRdf(webId: String, resource: SolidRDFResource, callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                webId,
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.delete(webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }

        override fun deleteContainer(webId: String, containerUrl: String, callback: IASSUnitCallback) {
            handleBasicExceptions(
                webId,
                containerUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.delete(webId, URI.create(containerUrl))) {
                        is SolidNetworkResponse.Success -> callback.onResult()
                        is SolidNetworkResponse.Error -> callback.onError(UNKNOWN, result.errorMessage)
                        is SolidNetworkResponse.Exception -> callback.onError(UNKNOWN, result.exception.message ?: "")
                    }
                }
            }
        }
    }
}
