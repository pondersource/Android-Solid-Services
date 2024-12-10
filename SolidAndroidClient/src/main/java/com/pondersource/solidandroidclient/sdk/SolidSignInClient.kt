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
import com.pondersource.solidandroidclient.IASSLogoutCallback
import com.pondersource.solidandroidclient.sdk.SolidException.SolidAppNotFoundException
import com.pondersource.solidandroidclient.sdk.SolidException.SolidNotLoggedInException
import com.pondersource.solidandroidclient.sdk.SolidException.SolidServiceConnectionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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
    private val connectionFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var iASSAuthService: IASSAuthenticatorService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSAuthService = IASSAuthenticatorService.Stub.asInterface(service)
            connectionFlow.value = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSAuthService = null
            connectionFlow.value = true
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

    fun authServiceConnectionState(): Flow<Boolean> {
        return connectionFlow
    }

    private fun hasConnectedToService() = iASSAuthService != null

    fun checkConnectionWithASS(onContinue: () -> Unit) {
        return if (hasInstalledAndroidSolidServices()) {
            if (hasConnectedToService()) {
                if(iASSAuthService!!.hasLoggedIn()) {
                    onContinue()
                } else {
                    throw SolidNotLoggedInException("Please login to your Solid account in Android Solid Services app.")
                }
            } else {
                throw SolidServiceConnectionException("Problem occurred while connecting to ASS app.")
            }
        } else {
            throw SolidAppNotFoundException("Please install Android Solid Services app on your device.")
        }
    }


    @Throws(Exception::class)
    fun getAccount() : SolidSignInAccount?{
        if (hasInstalledAndroidSolidServices()) {
            if (hasConnectedToService()) {
                if(iASSAuthService!!.hasLoggedIn()) {
                    return if(iASSAuthService!!.isAppAuthorized()) {
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
                throw SolidServiceConnectionException()
            }
        } else {
            throw SolidAppNotFoundException()
        }
    }

    fun requestLogin(callBack: (Boolean?, SolidException?) -> Unit) {
        checkConnectionWithASS {
            iASSAuthService!!.requestLogin(object : IASSLoginCallback.Stub() {
                override fun onResult(granted: Boolean) {
                    callBack(granted, null)
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callBack(null, handleSolidException(errorCode, errorMessage))
                }
            })
        }
    }

    fun disconnectFromSolid(callBack: (Boolean) -> Unit) {
        checkConnectionWithASS {
            iASSAuthService!!.disconnectFromSolid(object : IASSLogoutCallback.Stub() {
                override fun onResult(granted: Boolean) {
                    callBack(granted)
                }

                override fun onError(errorCode: kotlin.Int, errorMessage: kotlin.String?) {
                    //TODO("Not yet implemented")
                }
            })
        }
    }
}