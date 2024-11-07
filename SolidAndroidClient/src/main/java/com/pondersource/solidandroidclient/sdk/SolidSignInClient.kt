package com.pondersource.solidandroidclient.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.os.IBinder
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_AUTH_SERVICE
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.pondersource.solidandroidclient.IASSAuthenticatorService
import com.pondersource.solidandroidclient.IASSLoginCallback
import com.pondersource.solidandroidclient.sdk.ASSConnectionResponse.Success

class SolidSignInClient {

    companion object {
        @Volatile
        private lateinit var INSTANCE: SolidSignInClient

        /**
         *  get a single instance of the class
         *  @param context ApplicationContext
         *  @return SolidSignInClient object
         */
        fun getInstance(
            context: Context,
            applicationInfo: ApplicationInfo,
            hasInstalledAndroidSolidServices: () -> Boolean
        ): SolidSignInClient {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidSignInClient(context, applicationInfo, hasInstalledAndroidSolidServices)
                INSTANCE
            }
        }
    }

    private var applicationInfo: ApplicationInfo
    private val hasInstalledAndroidSolidServices: () -> Boolean
    private var iASSAuthService: IASSAuthenticatorService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSAuthService = IASSAuthenticatorService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSAuthService = null
        }
    }

    private constructor(
        context: Context,
        applicationInfo: ApplicationInfo,
        hasInstalledAndroidSolidServices: () -> Boolean
    ) {
        this.applicationInfo = applicationInfo
        this.hasInstalledAndroidSolidServices = hasInstalledAndroidSolidServices
        val intent = Intent().apply {
            setClassName(
                ANDROID_SOLID_SERVICES_PACKAGE_NAME,
                ANDROID_SOLID_SERVICES_AUTH_SERVICE
            )
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun hasConnectedToService() = iASSAuthService != null

    fun checkConnectionWithASS(): ASSConnectionResponse {
        return if (hasInstalledAndroidSolidServices()) {
            if (hasConnectedToService()) {
                if(iASSAuthService!!.hasLoggedIn()) {
                    Success(iASSAuthService!!.isAppAuthorized(applicationInfo.packageName))
                    /*val appInfo = context.applicationInfo
                    iASSAuthService!!.requestLogin(appInfo.packageName, appInfo.name, appInfo.icon)*/
                } else {
                    ASSConnectionResponse.SolidHasNotLoggedIn("Please login to your Solid account in Android Solid Services app.")
                }
            } else {
                ASSConnectionResponse.Error("Problem occurred while connecting to ASS app.")
            }
        } else {
            ASSConnectionResponse.AppNotFound("Please install Android Solid Services app on your device.")
        }
    }

    fun requestLogin() {
        when(val connection = checkConnectionWithASS()) {
            is Success -> {
                if (connection.accessIsGranted) {
                    //Access is already granted
                } else {
                    iASSAuthService!!.requestLogin(applicationInfo.packageName, applicationInfo.name, applicationInfo.icon, object : IASSLoginCallback.Stub() {
                        override fun onResult(granted: Boolean) {
                            //TODO
                        }
                    })
                }
            }
            else -> {

            }
        }
    }
}