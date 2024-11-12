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
import com.pondersource.solidandroidclient.sdk.SolidException.SolidAppNotFoundException
import com.pondersource.solidandroidclient.sdk.SolidException.SolidNotLoggedInException

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
    private var applicationName: String
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
        this.applicationName = context.packageManager.getApplicationLabel(this.applicationInfo).toString()
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


    @Throws(Exception::class)
    fun getAccount() : SolidSignInAccount?{
        if (hasInstalledAndroidSolidServices()) {
            if (hasConnectedToService()) {
                if(iASSAuthService!!.hasLoggedIn()) {
                    return if(iASSAuthService!!.isAppAuthorized(applicationInfo.packageName)) {
                        SolidSignInAccount(
                            applicationInfo.packageName
                        )
                    } else {
                        null
                    }
                } else {
                    throw SolidNotLoggedInException()
                }
            } else {
                throw Exception("Error while connecting to Solid Service.")
            }
        } else {
            throw SolidAppNotFoundException()
        }
    }

    fun requestLogin(callBack: (Boolean) -> Unit) {
        when(val connection = checkConnectionWithASS()) {
            is Success -> {
                if (connection.accessIsGranted) {
                    callBack(true)
                } else {
                    iASSAuthService!!.requestLogin(applicationInfo.packageName, applicationName, object : IASSLoginCallback.Stub() {
                        override fun onResult(granted: Boolean) {
                            callBack(granted)
                        }
                    })
                }
            }
            is ASSConnectionResponse.AppNotFound -> {
                throw SolidAppNotFoundException()
            }
            is ASSConnectionResponse.SolidHasNotLoggedIn -> {
                throw SolidNotLoggedInException()
            }
            is ASSConnectionResponse.Error -> {
                throw Exception("Unknown error occurred.")
            }
        }
    }
}