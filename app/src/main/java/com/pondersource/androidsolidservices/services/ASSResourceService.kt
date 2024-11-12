package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pondersource.androidsolidservices.model.PermissionType
import com.pondersource.androidsolidservices.repository.ResourcePermissionRepository
import com.pondersource.androidsolidservices.usecase.Authenticator
import com.pondersource.androidsolidservices.usecase.SolidResourceManager
import com.pondersource.solidandroidclient.IASSNonRdfResourceCallback
import com.pondersource.solidandroidclient.IASSRdfResourceCallback
import com.pondersource.solidandroidclient.IASSResourceService
import com.pondersource.solidandroidclient.NonRDFSource
import com.pondersource.solidandroidclient.RDFSource
import com.pondersource.solidandroidclient.SolidNetworkResponse
import com.pondersource.solidandroidclient.sdk.ExceptionsErrorCode.NOT_PERMISSION
import com.pondersource.solidandroidclient.sdk.ExceptionsErrorCode.SOLID_NOT_LOGGED_IN
import com.pondersource.solidandroidclient.sdk.ExceptionsErrorCode.UNKNOWN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@AndroidEntryPoint
class ASSResourceService: LifecycleService() {

    private val TAG = "ASSResourceService"
    @Inject
    lateinit var authenticator : Authenticator
    @Inject
    lateinit var solidResourceManager : SolidResourceManager

    @Inject
    lateinit var resourcePermissionRepository: ResourcePermissionRepository

    override fun onBind(intent: Intent): IBinder? {
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
                if (resourcePermissionRepository.hasAccess(callerPackage, resourceUrl, permissionType)) {
                    enterFunction()
                } else {
                    errorCallback(NOT_PERMISSION, "App does not have permission to access the resource.")
                }
            } else {
                errorCallback(SOLID_NOT_LOGGED_IN, "Solid app has not logged in.")
            }
        }

        override fun create(resource: NonRDFSource, callback: IASSNonRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.CREATE,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.create(resource)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(resource)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }

        override fun createRdf(resource: RDFSource, callback: IASSRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.CREATE,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.create(resource)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(resource)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }

        override fun read(
            resourceUrl: String,
            callback: IASSNonRdfResourceCallback
        ) {
            handleBasicExceptions(
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.read(URI.create(resourceUrl), NonRDFSource::class.java)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(result.data)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }

        override fun readRdf(
            resourceUrl: String,
            callback: IASSRdfResourceCallback
        ) {
            handleBasicExceptions(
                resourceUrl,
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.READ,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.read(URI.create(resourceUrl), RDFSource::class.java)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(result.data)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }

        override fun update(resource: NonRDFSource, callback: IASSNonRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.update(resource)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(resource)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }

        override fun updateRdf(resource: RDFSource, callback: IASSRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.UPDATE,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.update(resource)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(resource)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }

        override fun delete(resource: NonRDFSource, callback: IASSNonRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.delete(resource)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(resource)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }

        override fun deleteRdf(resource: RDFSource, callback: IASSRdfResourceCallback) {
            handleBasicExceptions(
                resource.getIdentifier().toString(),
                packageManager.getNameForUid(getCallingUid())!!,
                PermissionType.DELETE,
                { code, message ->
                    callback.onError(code, message)
                }
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = solidResourceManager.delete(resource)
                    when(result) {
                        is SolidNetworkResponse.Success -> {
                            callback.onResult(resource)
                        }
                        is  SolidNetworkResponse.Error-> {
                            callback.onError(UNKNOWN, result.errorMessage)
                        }
                        is  SolidNetworkResponse.Exception-> {
                            callback.onError(UNKNOWN, result.exception.message)
                        }
                    }
                }
            }
        }
    }
}