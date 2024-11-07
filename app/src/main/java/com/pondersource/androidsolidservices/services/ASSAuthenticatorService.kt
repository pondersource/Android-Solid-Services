package com.pondersource.androidsolidservices.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pondersource.androidsolidservices.usecase.Authenticator
import com.pondersource.androidsolidservices.usecase.SolidResourceManager
import com.pondersource.solidandroidclient.IASSAuthenticatorService
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
    lateinit var solidResourceManager : SolidResourceManager

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


   private val binder = object : IASSAuthenticatorService.Stub() {
        override fun hasLoggedIn(): Boolean {
            return authenticator.isUserAuthorized()
        }

        override fun isAppAuthorized(appPackageName: String?): Boolean {
            //TODO(Access local database of ASS to check if app has been granted before or no)
            return false
        }

       override fun requestLogin(appPackagename: String?, appName: String?, icon: Int) {
           //TODO
           CoroutineScope(Dispatchers.Main).launch {
               val builder = MaterialAlertDialogBuilder(this@ASSAuthenticatorService)
                   .setIcon(icon)
                   .setTitle("Login with Solid")
                   .setMessage("$appName wants to access to your Pod. Do you allow?")
                   .setNegativeButton("DENY", { dialog, which ->
                       Toast.makeText(this@ASSAuthenticatorService, "denied", Toast.LENGTH_SHORT).show()
                   })
                   .setPositiveButton("ALLOW", { dialog, which ->
                       Toast.makeText(this@ASSAuthenticatorService, "allowed", Toast.LENGTH_SHORT).show()
                   })

               val alert = builder.create()
               alert.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
               alert.show()
           }
       }
   }
}