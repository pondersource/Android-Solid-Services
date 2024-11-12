package com.pondersource.androidsolidservices.services

import com.pondersource.androidsolidservices.R
import android.app.AlertDialog
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import com.pondersource.androidsolidservices.usecase.Authenticator
import com.pondersource.solidandroidclient.IASSAuthenticatorService
import com.pondersource.solidandroidclient.IASSLoginCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ASSAuthenticatorService : LifecycleService(), SavedStateRegistryOwner {

    private val registryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = registryController.savedStateRegistry

    @Inject
    lateinit var authenticator : Authenticator

    @Inject
    lateinit var accessGrantRepository: AccessGrantRepository

    lateinit var alertDialog: AlertDialog.Builder

    override fun onCreate() {
        super.onCreate()
        registryController.performAttach()
        registryController.performRestore(null)
        setTheme(androidx.appcompat.R.style.AlertDialog_AppCompat)

        alertDialog = AlertDialog
            .Builder(this, R.style.Theme_AndroidSolidServices)
            .setCancelable(false)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private val binder = object : IASSAuthenticatorService.Stub() {
        override fun hasLoggedIn(): Boolean {
            return authenticator.isUserAuthorized()
        }

        override fun isAppAuthorized(appPackageName: String): Boolean {
            return accessGrantRepository.hasAccessGrant(appPackageName)
        }

        override fun requestLogin(appPackageName: String, appName: String, callback: IASSLoginCallback) {
            showLoginDialog(appPackageName, appName, callback)
        }
    }

    private fun showLoginDialog(
        appPackageName: String,
        appName: String,
        callback: IASSLoginCallback
    ) {

        ContextCompat.getMainExecutor(this).execute( {
            alertDialog
                .setTitle("Permission Request")
                .setMessage("$appName wants to access your Solid pod. Do you allow?")
                .setNegativeButton("Reject") { dialog, _ ->
                    accessGrantRepository.revokeAccessGrant(appPackageName)
                    dialog.dismiss()
                    callback.onResult(false)
                }
                .setPositiveButton("Allow") { dialog, _ ->
                    accessGrantRepository.addAccessGrant(appPackageName, appName)
                    dialog.dismiss()
                    callback.onResult(true)
                }
                .create().apply {
                    window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                    window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                    window!!.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }.show()
        })
    }
}