package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.pondersource.androidsolidservices.repository.AccessGrantRepository
import com.pondersource.androidsolidservices.ui.ProfileSelectionActivity
import com.pondersource.shared.domain.error.ExceptionsErrorCode.DRAW_OVERLAY_NOT_PERMITTED
import com.pondersource.shared.domain.error.ExceptionsErrorCode.SOLID_NOT_LOGGED_IN
import com.pondersource.solidandroidapi.Authenticator
import com.pondersource.shared.IASSAuthenticatorService
import com.pondersource.shared.domain.auth.IASSLoginCallback
import com.pondersource.shared.domain.auth.IASSLogoutCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class ASSAuthenticatorService : LifecycleService(), SavedStateRegistryOwner {

    private val registryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = registryController.savedStateRegistry

    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var accessGrantRepository: AccessGrantRepository

    override fun onCreate() {
        super.onCreate()
        registryController.performAttach()
        registryController.performRestore(null)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private val binder = object : IASSAuthenticatorService.Stub() {
        override fun hasLoggedIn(): Boolean {
            return authenticator.isUserAuthorized()
        }

        override fun isAppAuthorized(webId: String): Boolean {
            val packageName = packageManager.getNameForUid(getCallingUid())!!
            return accessGrantRepository.hasAccessGrant(packageName, webId)
        }

        override fun requestLogin(callback: IASSLoginCallback) {
            if (!Settings.canDrawOverlays(this@ASSAuthenticatorService)) {
                callback.onError(
                    DRAW_OVERLAY_NOT_PERMITTED,
                    "Android Solid Services doesn't have permission to draw overlay. Please ask the user to enable it in app settings."
                )
                return
            }
            if (!hasLoggedIn()) {
                callback.onError(SOLID_NOT_LOGGED_IN, "User has not logged in.")
                return
            }

            val packageName = packageManager.getNameForUid(getCallingUid())!!
            val appName = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()

            val requestId = UUID.randomUUID().toString()
            PendingLoginRequests.put(
                requestId,
                PendingLoginRequest(
                    callerPackage = packageName,
                    callerName = appName,
                    callback = callback,
                )
            )

            val intent = Intent(this@ASSAuthenticatorService, ProfileSelectionActivity::class.java).apply {
                putExtra(ProfileSelectionActivity.EXTRA_REQUEST_ID, requestId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        override fun disconnectFromSolid(webId: String, callback: IASSLogoutCallback) {
            val packageName = packageManager.getNameForUid(getCallingUid())!!
            lifecycleScope.launch {
                accessGrantRepository.revokeAccessGrant(packageName, webId)
                callback.onResult(true)
            }
        }
    }
}
