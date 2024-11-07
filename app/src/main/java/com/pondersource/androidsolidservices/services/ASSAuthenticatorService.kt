package com.pondersource.androidsolidservices.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import com.pondersource.androidsolidservices.usecase.Authenticator
import com.pondersource.androidsolidservices.usecase.SolidResourceManager
import com.pondersource.solidandroidclient.IASSAuthenticatorService
import com.pondersource.solidandroidclient.IASSLoginCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ASSAuthenticatorService: Service() {

    @Inject
    lateinit var authenticator : Authenticator

    @Inject
    lateinit var accessGrantRepository: AccessGrantRepository

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


   private val binder = object : IASSAuthenticatorService.Stub() {
        override fun hasLoggedIn(): Boolean {
            return authenticator.isUserAuthorized()
        }

        override fun isAppAuthorized(appPackageName: String): Boolean {
            return accessGrantRepository.hasAccessGrant(appPackageName)
        }

       override fun requestLogin(appPackagename: String, appName: String, icon: Int, callback: IASSLoginCallback) {
           //TODO
           val builder = MaterialAlertDialogBuilder(this@ASSAuthenticatorService)
               .setIcon(icon)
               .setTitle("Login with Solid")
               .setMessage("$appName wants to access to your Pod. Do you allow?")
               .setNegativeButton("DENY", { dialog, which ->
                   accessGrantRepository.revokeAccessGrant(appPackagename)
                   callback.onResult(false)
                   Toast.makeText(this@ASSAuthenticatorService, "denied", Toast.LENGTH_SHORT).show()
               })
               .setPositiveButton("ALLOW", { dialog, which ->
                   accessGrantRepository.addAccessGrant(appPackagename, appName, icon)
                   callback.onResult(true)
                   Toast.makeText(this@ASSAuthenticatorService, "allowed", Toast.LENGTH_SHORT).show()
               })

           val alert = builder.create()
           alert.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
           alert.show()
       }
   }
}