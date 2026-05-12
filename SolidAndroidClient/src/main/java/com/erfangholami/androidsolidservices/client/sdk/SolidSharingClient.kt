package com.erfangholami.androidsolidservices.client.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.erfangholami.androidsolidservices.shared.IASSharingService
import com.erfangholami.androidsolidservices.shared.domain.IASSUnitCallback
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
import com.erfangholami.androidsolidservices.client.internal.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.erfangholami.androidsolidservices.client.internal.ANDROID_SOLID_SERVICES_SHARING_SERVICE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Client SDK for the SolidShare sharing feature.
 *
 * Mirrors [com.erfangholami.androidsolidservices.api.sharing.SharingManager] over AIDL,
 * binding to the `ASSSharingService` exported by the Android Solid Services app.
 */
public class SolidSharingClient {

    public companion object {
        @Volatile
        private var INSTANCE: SolidSharingClient? = null

        public fun getInstance(context: Context): SolidSharingClient =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SolidSharingClient(context).also { INSTANCE = it }
            }
    }

    private var service: IASSharingService? = null
    private val connectionFlow = MutableStateFlow(false)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = IASSharingService.Stub.asInterface(binder)
            connectionFlow.value = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            connectionFlow.value = false
        }
    }

    private constructor(context: Context) {
        val intent = Intent().apply {
            setClassName(
                ANDROID_SOLID_SERVICES_PACKAGE_NAME,
                ANDROID_SOLID_SERVICES_SHARING_SERVICE,
            )
        }
        context.applicationContext
            .bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    public fun connectionState(): Flow<Boolean> = connectionFlow

    private fun requireService(): IASSharingService =
        service ?: throw SolidException.SolidServiceConnectionException()

    // ── Given shares ────────────────────────────────────────────────────────

    public suspend fun getStoredGivenShares(webId: String): List<GivenShare> =
        givenList { cb -> requireService().getStoredGivenShares(webId, cb) }

    public suspend fun refreshGivenShares(webId: String): List<GivenShare> =
        givenList { cb -> requireService().refreshGivenShares(webId, cb) }

    public suspend fun getGivenSharesForResource(
        webId: String,
        resourceUri: String,
    ): List<GivenShare> =
        givenList { cb -> requireService().getGivenSharesForResource(webId, resourceUri, cb) }

    public suspend fun createShare(
        webId: String,
        resourceUri: String,
        mode: ShareMode,
        receiver: ShareReceiver,
    ): GivenShare? = given { cb ->
        requireService().createShare(
            webId, resourceUri, mode.ordinal, receiver.kind(), receiver.value(), cb,
        )
    }

    public suspend fun updateShare(
        webId: String,
        resourceUri: String,
        mode: ShareMode,
        receiver: ShareReceiver,
    ): GivenShare? = given { cb ->
        requireService().updateShare(
            webId, resourceUri, mode.ordinal, receiver.kind(), receiver.value(), cb,
        )
    }

    public suspend fun revokeShare(
        webId: String,
        resourceUri: String,
        receiver: ShareReceiver,
    ): Unit = unit { cb ->
        requireService().revokeShare(
            webId, resourceUri, receiver.kind(), receiver.value(), cb,
        )
    }

    // ── Profile share ───────────────────────────────────────────────────────

    public suspend fun createProfileShare(
        webId: String,
        config: ProfileShareConfig,
        mode: ShareMode = ShareMode.READ,
    ): GivenShare? = given { cb ->
        requireService().createProfileShare(
            webId,
            config.selectedFields.map(ProfileField::predicate),
            mode.ordinal,
            config.receiver.kind(),
            config.receiver.value(),
            cb,
        )
    }

    // ── Received shares ─────────────────────────────────────────────────────

    public suspend fun getStoredReceivedShares(webId: String): List<ReceivedShare> =
        receivedList { cb -> requireService().getStoredReceivedShares(webId, cb) }

    public suspend fun refreshReceivedShares(webId: String): List<ReceivedShare> =
        receivedList { cb -> requireService().refreshReceivedShares(webId, cb) }

    public suspend fun addReceivedShare(
        webId: String,
        resourceUri: String,
    ): ReceivedShare? = received { cb ->
        requireService().addReceivedShare(webId, resourceUri, cb)
    }

    public suspend fun removeReceivedShare(
        webId: String,
        resourceUri: String,
        ownerWebId: String,
    ): Unit = unit { cb ->
        requireService().removeReceivedShare(webId, resourceUri, ownerWebId, cb)
    }

    // ── Continuation helpers ────────────────────────────────────────────────

    private suspend inline fun givenList(
        crossinline call: (IASSGivenShareListCallback) -> Unit,
    ): List<GivenShare> = suspendCancellableCoroutine { cont ->
        call(object : IASSGivenShareListCallback.Stub() {
            override fun onResult(shares: MutableList<GivenShare>?) {
                cont.resume(shares ?: emptyList())
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                cont.resumeWithException(handleSolidException(errorCode, errorMessage))
            }
        })
    }

    private suspend inline fun receivedList(
        crossinline call: (IASSReceivedShareListCallback) -> Unit,
    ): List<ReceivedShare> = suspendCancellableCoroutine { cont ->
        call(object : IASSReceivedShareListCallback.Stub() {
            override fun onResult(shares: MutableList<ReceivedShare>?) {
                cont.resume(shares ?: emptyList())
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                cont.resumeWithException(handleSolidException(errorCode, errorMessage))
            }
        })
    }

    private suspend inline fun given(
        crossinline call: (IASSGivenShareCallback) -> Unit,
    ): GivenShare? = suspendCancellableCoroutine { cont ->
        call(object : IASSGivenShareCallback.Stub() {
            override fun onResult(share: GivenShare?) {
                cont.resume(share)
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                cont.resumeWithException(handleSolidException(errorCode, errorMessage))
            }
        })
    }

    private suspend inline fun received(
        crossinline call: (IASSReceivedShareCallback) -> Unit,
    ): ReceivedShare? = suspendCancellableCoroutine { cont ->
        call(object : IASSReceivedShareCallback.Stub() {
            override fun onResult(share: ReceivedShare?) {
                cont.resume(share)
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                cont.resumeWithException(handleSolidException(errorCode, errorMessage))
            }
        })
    }

    private suspend inline fun unit(
        crossinline call: (IASSUnitCallback) -> Unit,
    ): Unit = suspendCancellableCoroutine { cont ->
        call(object : IASSUnitCallback.Stub() {
            override fun onResult() {
                cont.resume(Unit)
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                cont.resumeWithException(handleSolidException(errorCode, errorMessage))
            }
        })
    }
}
