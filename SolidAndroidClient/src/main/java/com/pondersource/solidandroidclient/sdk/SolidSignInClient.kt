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

/**
 * Manages sign-in authorization between a third-party app and the Android Solid Services app.
 *
 * Obtain an instance via [Solid.getSignInClient].  All operations require the Android Solid
 * Services app to be installed and running on the device.
 *
 * Typical flow:
 * 1. Check [authServiceConnectionState] to confirm the IPC service is connected.
 * 2. Call [getAccount] — if it returns `null`, the app is not yet authorized.
 * 3. Call [requestLogin] to prompt the user to grant access.
 * 4. Use [disconnectFromSolid] to revoke access when the user signs out.
 */
class SolidSignInClient {

    companion object {
        @Volatile
        private var INSTANCE: SolidSignInClient? = null

        /**
         * Returns the application-scoped singleton [SolidSignInClient].
         * @param context Any [Context]; the application context is used internally.
         * @param applicationInfo The calling app's [android.content.pm.ApplicationInfo], used
         *   to identify the app when requesting access.
         * @param hasInstalledAndroidSolidServices A lambda that returns `true` when the
         *   Android Solid Services app is installed on the device.
         */
        fun getInstance(
            context: Context,
            applicationInfo: ApplicationInfo,
            hasInstalledAndroidSolidServices: () -> Boolean
        ): SolidSignInClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SolidSignInClient(context, applicationInfo, hasInstalledAndroidSolidServices).also { INSTANCE = it }
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
            connectionFlow.value = false
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

    /**
     * Hot [Flow] of the IPC service connection state.
     * Emits `true` once the bound service connects and `false` if it disconnects.
     */
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


    /**
     * Returns a [SolidSignInAccount] if this app is authorized, or `null` if it has not yet
     * been granted access by the user.
     * @throws SolidException.SolidAppNotFoundException if the ASS app is not installed.
     * @throws SolidException.SolidServiceConnectionException if the IPC service is not connected.
     * @throws SolidException.SolidNotLoggedInException if no user is logged in.
     */
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

    /**
     * Requests the user to grant this app access to Solid via the ASS app.
     *
     * A system dialog is shown to the user inside the ASS app.  The result is delivered
     * asynchronously to [callBack]:
     * - `(true, null)` — access granted
     * - `(false, null)` — access denied by the user
     * - `(null, error)` — an error occurred
     */
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

    /**
     * Revokes this app's access to Solid.  [callBack] receives `true` on success.
     */
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