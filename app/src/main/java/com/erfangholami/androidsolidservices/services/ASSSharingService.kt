package com.erfangholami.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.erfangholami.androidsolidservices.shared.IASSharingService
import com.erfangholami.androidsolidservices.shared.domain.IASSUnitCallback
import com.erfangholami.androidsolidservices.shared.domain.error.ExceptionsErrorCode
import com.erfangholami.androidsolidservices.shared.domain.network.SolidNetworkResponse
import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSGivenShareCallback
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSGivenShareListCallback
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSReceivedShareCallback
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSReceivedShareListCallback
import com.erfangholami.androidsolidservices.shared.domain.sharing.ProfileField
import com.erfangholami.androidsolidservices.shared.domain.sharing.ProfileShareConfig
import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareMode
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareReceiver
import com.erfangholami.androidsolidservices.api.sharing.SharingManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ASSSharingService : LifecycleService() {

    @Inject
    lateinit var sharingManager: SharingManager

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private val binder = object : IASSharingService.Stub() {

        override fun getStoredGivenShares(
            webId: String,
            callback: IASSGivenShareListCallback,
        ) = launchGivenList(callback) { sharingManager.getStoredGivenShares(webId) }

        override fun refreshGivenShares(
            webId: String,
            callback: IASSGivenShareListCallback,
        ) = launchGivenList(callback) { sharingManager.refreshGivenShares(webId) }

        override fun getGivenSharesForResource(
            webId: String,
            resourceUri: String,
            callback: IASSGivenShareListCallback,
        ) = launchGivenList(callback) {
            sharingManager.getGivenSharesForResource(webId, resourceUri)
        }

        override fun createShare(
            webId: String,
            resourceUri: String,
            mode: Int,
            receiverKind: Int,
            receiverValue: String?,
            callback: IASSGivenShareCallback,
        ) = launchGiven(callback) {
            sharingManager.createShare(
                webId = webId,
                resourceUri = resourceUri,
                mode = ShareMode.entries[mode],
                receiver = ShareReceiver.fromKind(receiverKind, receiverValue),
            )
        }

        override fun updateShare(
            webId: String,
            resourceUri: String,
            mode: Int,
            receiverKind: Int,
            receiverValue: String?,
            callback: IASSGivenShareCallback,
        ) = launchGiven(callback) {
            sharingManager.updateShare(
                webId = webId,
                resourceUri = resourceUri,
                mode = ShareMode.entries[mode],
                receiver = ShareReceiver.fromKind(receiverKind, receiverValue),
            )
        }

        override fun revokeShare(
            webId: String,
            resourceUri: String,
            receiverKind: Int,
            receiverValue: String?,
            callback: IASSUnitCallback,
        ) = launchUnit(callback) {
            sharingManager.revokeShare(
                webId = webId,
                resourceUri = resourceUri,
                receiver = ShareReceiver.fromKind(receiverKind, receiverValue),
            )
        }

        override fun createProfileShare(
            webId: String,
            selectedFieldPredicates: List<String>,
            mode: Int,
            receiverKind: Int,
            receiverValue: String?,
            callback: IASSGivenShareCallback,
        ) = launchGiven(callback) {
            val fields = selectedFieldPredicates
                .mapNotNull { ProfileField.fromPredicate(it) }
                .toSet()
            sharingManager.createProfileShare(
                webId = webId,
                config = ProfileShareConfig(
                    selectedFields = fields,
                    receiver = ShareReceiver.fromKind(receiverKind, receiverValue),
                ),
                mode = ShareMode.entries[mode],
            )
        }

        override fun getStoredReceivedShares(
            webId: String,
            callback: IASSReceivedShareListCallback,
        ) = launchReceivedList(callback) { sharingManager.getStoredReceivedShares(webId) }

        override fun refreshReceivedShares(
            webId: String,
            callback: IASSReceivedShareListCallback,
        ) = launchReceivedList(callback) { sharingManager.refreshReceivedShares(webId) }

        override fun addReceivedShare(
            webId: String,
            resourceUri: String,
            callback: IASSReceivedShareCallback,
        ) = launchReceived(callback) {
            sharingManager.addReceivedShare(webId, resourceUri)
        }

        override fun removeReceivedShare(
            webId: String,
            resourceUri: String,
            ownerWebId: String,
            callback: IASSUnitCallback,
        ) = launchUnit(callback) {
            sharingManager.removeReceivedShare(webId, resourceUri, ownerWebId)
        }
    }

    // ── Dispatch helpers ────────────────────────────────────────────────────

    private inline fun launchGivenList(
        callback: IASSGivenShareListCallback,
        crossinline block: suspend () -> SolidNetworkResponse<List<GivenShare>>,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val r = block()) {
                is SolidNetworkResponse.Success -> callback.onResult(r.data)
                is SolidNetworkResponse.Error -> callback.onError(r.errorCode, r.errorMessage)
                is SolidNetworkResponse.Exception -> callback.onError(
                    ExceptionsErrorCode.UNKNOWN, r.exception.message ?: "Unknown error",
                )
            }
        }
    }

    private inline fun launchReceivedList(
        callback: IASSReceivedShareListCallback,
        crossinline block: suspend () -> SolidNetworkResponse<List<ReceivedShare>>,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val r = block()) {
                is SolidNetworkResponse.Success -> callback.onResult(r.data)
                is SolidNetworkResponse.Error -> callback.onError(r.errorCode, r.errorMessage)
                is SolidNetworkResponse.Exception -> callback.onError(
                    ExceptionsErrorCode.UNKNOWN, r.exception.message ?: "Unknown error",
                )
            }
        }
    }

    private inline fun launchGiven(
        callback: IASSGivenShareCallback,
        crossinline block: suspend () -> SolidNetworkResponse<GivenShare>,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val r = block()) {
                is SolidNetworkResponse.Success -> callback.onResult(r.data)
                is SolidNetworkResponse.Error -> callback.onError(r.errorCode, r.errorMessage)
                is SolidNetworkResponse.Exception -> callback.onError(
                    ExceptionsErrorCode.UNKNOWN, r.exception.message ?: "Unknown error",
                )
            }
        }
    }

    private inline fun launchReceived(
        callback: IASSReceivedShareCallback,
        crossinline block: suspend () -> SolidNetworkResponse<ReceivedShare>,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val r = block()) {
                is SolidNetworkResponse.Success -> callback.onResult(r.data)
                is SolidNetworkResponse.Error -> callback.onError(r.errorCode, r.errorMessage)
                is SolidNetworkResponse.Exception -> callback.onError(
                    ExceptionsErrorCode.UNKNOWN, r.exception.message ?: "Unknown error",
                )
            }
        }
    }

    private inline fun launchUnit(
        callback: IASSUnitCallback,
        crossinline block: suspend () -> SolidNetworkResponse<Unit>,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val r = block()) {
                is SolidNetworkResponse.Success -> callback.onResult()
                is SolidNetworkResponse.Error -> callback.onError(r.errorCode, r.errorMessage)
                is SolidNetworkResponse.Exception -> callback.onError(
                    ExceptionsErrorCode.UNKNOWN, r.exception.message ?: "Unknown error",
                )
            }
        }
    }
}
