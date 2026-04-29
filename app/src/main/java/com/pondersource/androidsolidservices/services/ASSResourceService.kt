package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pondersource.androidsolidservices.model.PermissionType
import com.pondersource.androidsolidservices.repository.ResourcePermissionRepository
import com.pondersource.shared.domain.error.ExceptionsErrorCode.NOT_PERMISSION
import com.pondersource.shared.domain.error.ExceptionsErrorCode.NULL_WEBID
import com.pondersource.shared.domain.error.ExceptionsErrorCode.SOLID_NOT_LOGGED_IN
import com.pondersource.shared.domain.error.ExceptionsErrorCode.UNKNOWN
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.SolidNonRDFResource
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.solidandroidapi.Authenticator
import com.pondersource.solidandroidapi.SolidResourceManager
import com.pondersource.solidandroidclient.IASSResourceService
import com.pondersource.solidandroidclient.IASSSolidNonRdfResourceCallback
import com.pondersource.solidandroidclient.IASSSolidRdfResourceCallback
import com.pondersource.solidandroidclient.IASSUnitCallback
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

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private val binder = object : IASSResourceService.Stub() {

        private fun handleBasicExceptions(
            resourceUrl: String,
            callerPackage: String,
            permissionType: PermissionType,
            errorCallback: (Int, String) -> Unit,
            enterFunction: () -> Unit,
        ) {
            if (authenticator.isUserAuthorized()) {
                if (resourcePermissionRepository.hasAccess(
                        callerPackage,
                        resourceUrl,
                        permissionType
                    )
                ) {
                    enterFunction()
                } else {
                    errorCallback(
                        NOT_PERMISSION,
                        "App does not have permission to access the resource."
                    )
                }
            } else {
                errorCallback(SOLID_NOT_LOGGED_IN, "Solid app has not logged in.")
            }
        }

        private fun getProfile() = authenticator.getActiveProfile()

        override fun getWebId(callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                "",
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                val webId = getProfile().webId
                if (webId != null) {
                    callback.onResult(webId)
                } else {
                    callback.onError(NULL_WEBID, "WebID is null.")
                }
            }
        }

        override fun create(
            resource: SolidNonRDFResource,
            callback: IASSSolidNonRdfResourceCallback
        ) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.CREATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result =
                        solidResourceManager.create(getProfile().userInfo!!.webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun createRdf(resource: SolidRDFResource, callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.CREATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result =
                        solidResourceManager.create(getProfile().userInfo!!.webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun read(resourceUrl: String, callback: IASSSolidNonRdfResourceCallback) {
            handleBasicExceptions(
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.read(
                        getProfile().userInfo!!.webId,
                        URI.create(resourceUrl),
                        SolidNonRDFResource::class.java,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult(result.data)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun readRdf(resourceUrl: String, callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.read(
                        getProfile().userInfo!!.webId,
                        URI.create(resourceUrl),
                        SolidRDFResource::class.java,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult(result.data)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun update(
            resource: SolidNonRDFResource,
            ifMatch: String?,
            callback: IASSSolidNonRdfResourceCallback,
        ) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.update(
                        getProfile().userInfo!!.webId,
                        resource,
                        ifMatch,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun updateRdf(
            resource: SolidRDFResource,
            ifMatch: String?,
            callback: IASSSolidRdfResourceCallback,
        ) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.update(
                        getProfile().userInfo!!.webId,
                        resource,
                        ifMatch,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun patch(resourceUrl: String, patchBody: String, callback: IASSUnitCallback) {
            handleBasicExceptions(
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.patchRaw(
                        getProfile().userInfo!!.webId,
                        URI.create(resourceUrl),
                        patchBody,
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult()
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun delete(
            resource: SolidNonRDFResource,
            callback: IASSSolidNonRdfResourceCallback
        ) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result =
                        solidResourceManager.delete(getProfile().userInfo!!.webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun deleteRdf(resource: SolidRDFResource, callback: IASSSolidRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result =
                        solidResourceManager.delete(getProfile().userInfo!!.webId, resource)) {
                        is SolidNetworkResponse.Success -> callback.onResult(resource)
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }

        override fun deleteContainer(containerUrl: String, callback: IASSUnitCallback) {
            handleBasicExceptions(
                containerUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message -> callback.onError(code, message) }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (val result = solidResourceManager.deleteContainer(
                        getProfile().userInfo!!.webId,
                        URI.create(containerUrl),
                    )) {
                        is SolidNetworkResponse.Success -> callback.onResult()
                        is SolidNetworkResponse.Error -> callback.onError(
                            UNKNOWN,
                            result.errorMessage
                        )

                        is SolidNetworkResponse.Exception -> callback.onError(
                            UNKNOWN,
                            result.exception.message ?: ""
                        )
                    }
                }
            }
        }
    }
}
