package com.pondersource.solidandroidclient.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_AUTH_SERVICE
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.pondersource.solidandroidclient.IASSAuthenticatorService

class SolidSignInClient {

    companion object {
        @Volatile
        private lateinit var INSTANCE: SolidSignInClient

        /**
         *  get a single instance of the class
         *  @param context ApplicationContext
         *  @return SolidSignInClient object
         */
        fun getInstance(context: Context): SolidSignInClient {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidSignInClient(context)
                INSTANCE
            }
        }
    }

    private val context: Context
    private var iASSAuthService: IASSAuthenticatorService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSAuthService = IASSAuthenticatorService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSAuthService = null
        }
    }

    private constructor(context: Context) {
        this.context = context
        val intent = Intent().apply {
            setClassName(
                ANDROID_SOLID_SERVICES_PACKAGE_NAME,
                ANDROID_SOLID_SERVICES_AUTH_SERVICE
            )
        }
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun hasConnectedToService() = iASSAuthService != null

    fun requestLogin() {
        if (hasInstalledAndroidSolidServices(context)) {
            if (iASSAuthService != null) {
                if(iASSAuthService!!.hasLoggedIn()) {
                    val appInfo = context.applicationInfo
                    iASSAuthService!!.requestLogin(appInfo.packageName, appInfo.name, appInfo.icon)
                } else {
                    //Ask user to login in ASS app
                }
            } else {
                //Error while connecting to ASSAuthenticatorService
            }
        } else {
            //show to user that they haven't installed Android Solid Services
        }
    }
}