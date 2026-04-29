package com.pondersource.solidandroidclient.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.profile.WebId
import com.pondersource.shared.domain.resource.NonRDFResource
import com.pondersource.shared.domain.resource.RDFResource
import com.pondersource.shared.domain.resource.SolidNonRDFResource
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.domain.resource.SolidResource
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_CRUD_SERVICE
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.pondersource.solidandroidclient.IASSResourceService
import com.pondersource.solidandroidclient.IASSSolidNonRdfResourceCallback
import com.pondersource.solidandroidclient.IASSSolidRdfResourceCallback
import com.pondersource.solidandroidclient.IASSUnitCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Headers
import java.io.InputStream
import java.net.URI
import kotlin.coroutines.resume

/**
 * Reads, creates, updates and deletes resources on the authenticated user's Solid pod by
 * communicating with the Android Solid Services app over IPC.
 *
 * Obtain an instance via [Solid.getResourceClient].
 *
 * All public methods are `suspend` functions and must be called from a coroutine.  Results are
 * wrapped in [SolidNetworkResponse] — callers can use `when`, [SolidNetworkResponse.getOrThrow],
 * [SolidNetworkResponse.getOrNull], or [SolidNetworkResponse.getOrDefault] to handle outcomes.
 * Precondition failures (app not installed, service not connected) are returned as
 * [SolidNetworkResponse.Exception].
 *
 * @see Solid.getResourceClient
 */
class SolidResourceClient {

    companion object {
        @Volatile
        private var INSTANCE: SolidResourceClient? = null

        /**
         * Returns the application-scoped singleton [SolidResourceClient].
         * @param context Any [Context]; the application context is used internally.
         * @param hasInstalledAndroidSolidServices A lambda that returns `true` when the
         *   Android Solid Services app is installed on the device.
         */
        fun getInstance(
            context: Context,
            hasInstalledAndroidSolidServices: () -> Boolean
        ): SolidResourceClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SolidResourceClient(
                    context,
                    hasInstalledAndroidSolidServices
                ).also { INSTANCE = it }
            }
        }
    }

    private var iASSResourceService: IASSResourceService? = null
    private val connectionFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val hasInstalledAndroidSolidServices: () -> Boolean

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSResourceService = IASSResourceService.Stub.asInterface(service)
            connectionFlow.value = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSResourceService = null
            connectionFlow.value = false
        }
    }

    private constructor(
        context: Context,
        hasInstalledAndroidSolidServices: () -> Boolean
    ) {
        this.hasInstalledAndroidSolidServices = hasInstalledAndroidSolidServices
        val intent = Intent().apply {
            setClassName(ANDROID_SOLID_SERVICES_PACKAGE_NAME, ANDROID_SOLID_SERVICES_CRUD_SERVICE)
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Hot [Flow] of the IPC service connection state.
     * Emits `true` once the bound service connects and `false` if it disconnects.
     */
    fun resourceServiceConnectionState(): Flow<Boolean> = connectionFlow

    private fun checkBasicConditions(): SolidNetworkResponse<Nothing>? {
        if (!hasInstalledAndroidSolidServices())
            return SolidNetworkResponse.Exception(SolidException.SolidAppNotFoundException())
        if (iASSResourceService == null)
            return SolidNetworkResponse.Exception(SolidException.SolidServiceConnectionException())
        return null
    }

    // ── Reconstruction helpers ────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun <T : SolidResource> reconstructRdf(source: SolidRDFResource, clazz: Class<T>): T {
        if (clazz.isInstance(source)) return source as T
        return clazz.getConstructor(
            URI::class.java, MediaType::class.java, List::class.java, Headers::class.java
        ).newInstance(
            source.getIdentifier(),
            MediaType.of(source.getContentType()),
            source.getAllQuads(),
            source.getHeaders()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : SolidResource> reconstructNonRdf(
        source: SolidNonRDFResource,
        clazz: Class<T>
    ): T {
        if (clazz.isInstance(source)) return source as T
        return clazz.getConstructor(
            URI::class.java, String::class.java, Headers::class.java, InputStream::class.java
        ).newInstance(
            source.getIdentifier(), source.getContentType(), source.getHeaders(), source.getEntity()
        )
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Fetches and parses the authenticated user's WebID document from their pod.
     * @return [SolidNetworkResponse.Success] with the [WebId], or an error/exception variant.
     */
    suspend fun getWebId(): SolidNetworkResponse<WebId> {
        checkBasicConditions()?.let {
            @Suppress("UNCHECKED_CAST")
            return it as SolidNetworkResponse<WebId>
        }
        return suspendCancellableCoroutine { cont ->
            iASSResourceService!!.getWebId(object : IASSSolidRdfResourceCallback.Stub() {
                override fun onResult(result: SolidRDFResource) {
                    try {
                        cont.resume(
                            SolidNetworkResponse.Success(
                                reconstructRdf(
                                    result,
                                    WebId::class.java
                                )
                            )
                        )
                    } catch (e: Exception) {
                        cont.resume(SolidNetworkResponse.Exception(e))
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                }
            })
        }
    }

    /**
     * Reads a resource from the pod.
     * @param resourceUrl The full URL of the resource.
     * @param clazz The expected resource type; must extend [RDFResource] or [NonRDFResource].
     * @return [SolidNetworkResponse.Success] with the resource, or an error/exception variant.
     */
    suspend fun <T : SolidResource> read(
        resourceUrl: String,
        clazz: Class<T>
    ): SolidNetworkResponse<T> {
        checkBasicConditions()?.let {
            @Suppress("UNCHECKED_CAST")
            return it as SolidNetworkResponse<T>
        }
        return suspendCancellableCoroutine { cont ->
            when {
                RDFResource::class.java.isAssignableFrom(clazz) -> {
                    iASSResourceService!!.readRdf(
                        resourceUrl,
                        object : IASSSolidRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructRdf(
                                                result,
                                                clazz
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                NonRDFResource::class.java.isAssignableFrom(clazz) -> {
                    iASSResourceService!!.read(
                        resourceUrl,
                        object : IASSSolidNonRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidNonRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructNonRdf(
                                                result,
                                                clazz
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                else -> {
                    cont.resume(
                        SolidNetworkResponse.Exception(
                            IllegalArgumentException("Class must extend RDFResource or NonRDFResource.")
                        )
                    )
                }
            }
        }
    }

    /**
     * Creates a new resource on the pod at the URI specified by [resource].
     * @return [SolidNetworkResponse.Success] with the created resource.
     */
    suspend fun <T : SolidResource> create(resource: T): SolidNetworkResponse<T> {
        checkBasicConditions()?.let {
            @Suppress("UNCHECKED_CAST")
            return it as SolidNetworkResponse<T>
        }
        return suspendCancellableCoroutine { cont ->
            when (resource) {
                is SolidRDFResource -> {
                    iASSResourceService!!.createRdf(
                        resource,
                        object : IASSSolidRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructRdf(
                                                result,
                                                resource.javaClass
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                is SolidNonRDFResource -> {
                    iASSResourceService!!.create(
                        resource,
                        object : IASSSolidNonRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidNonRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructNonRdf(
                                                result,
                                                resource.javaClass
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                else -> cont.resume(
                    SolidNetworkResponse.Exception(
                        IllegalArgumentException("Resource must be SolidRDFResource or SolidNonRDFResource.")
                    )
                )
            }
        }
    }

    /**
     * Replaces an existing resource on the pod via HTTP PUT.
     *
     * Pass [ifMatch] (the ETag from a previous [read]) to issue a conditional PUT that fails
     * with a 412 error if the resource was modified concurrently.  This is the safe update
     * pattern for [SolidNonRDFResource] (binary files, plain text, etc.).
     *
     * For RDF resources, prefer [patch] when only a subset of triples changes — it is atomic
     * and avoids a full read-modify-write cycle.
     *
     * @param resource    The updated resource; its identifier determines the target URI.
     * @param ifMatch     Optional ETag for a conditional PUT.
     * @return [SolidNetworkResponse.Success] with the updated resource.
     */
    suspend fun <T : SolidResource> update(
        resource: T,
        ifMatch: String? = null
    ): SolidNetworkResponse<T> {
        checkBasicConditions()?.let {
            @Suppress("UNCHECKED_CAST")
            return it as SolidNetworkResponse<T>
        }
        return suspendCancellableCoroutine { cont ->
            when (resource) {
                is SolidRDFResource -> {
                    iASSResourceService!!.updateRdf(
                        resource,
                        ifMatch,
                        object : IASSSolidRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructRdf(
                                                result,
                                                resource.javaClass
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                is SolidNonRDFResource -> {
                    iASSResourceService!!.update(
                        resource,
                        ifMatch,
                        object : IASSSolidNonRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidNonRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructNonRdf(
                                                result,
                                                resource.javaClass
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                else -> cont.resume(
                    SolidNetworkResponse.Exception(
                        IllegalArgumentException("Resource must be SolidRDFResource or SolidNonRDFResource.")
                    )
                )
            }
        }
    }

    /**
     * Applies an N3 Patch to an RDF resource on the pod via HTTP PATCH.
     *
     * This is the preferred method for partial updates to RDF resources — it is atomic and
     * does not require reading the full resource first.  Use [N3Patch.build] or
     * [N3Patch.fromDiff] to construct the patch without writing raw N3 strings.
     *
     * Not applicable to [SolidNonRDFResource] — use [update] for binary resources.
     *
     * @param uri   The URI of the RDF resource to patch.
     * @param patch The patch to apply.
     * @return [SolidNetworkResponse.Success] with [Unit] on success.
     */
    suspend fun patch(uri: URI, patch: N3Patch): SolidNetworkResponse<Unit> {
        checkBasicConditions()?.let {
            @Suppress("UNCHECKED_CAST")
            return it as SolidNetworkResponse<Unit>
        }
        return suspendCancellableCoroutine { cont ->
            iASSResourceService!!.patch(
                uri.toString(),
                patch.toN3String(),
                object : IASSUnitCallback.Stub() {
                    override fun onResult() {
                        cont.resume(SolidNetworkResponse.Success(Unit))
                    }

                    override fun onError(errorCode: Int, errorMessage: String) {
                        cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                    }
                }
            )
        }
    }

    /**
     * Deletes a resource from the pod.
     * @return [SolidNetworkResponse.Success] with the deleted resource.
     */
    suspend fun <T : SolidResource> delete(resource: T): SolidNetworkResponse<T> {
        checkBasicConditions()?.let {
            @Suppress("UNCHECKED_CAST")
            return it as SolidNetworkResponse<T>
        }
        return suspendCancellableCoroutine { cont ->
            when (resource) {
                is SolidRDFResource -> {
                    iASSResourceService!!.deleteRdf(
                        resource,
                        object : IASSSolidRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructRdf(
                                                result,
                                                resource.javaClass
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                is SolidNonRDFResource -> {
                    iASSResourceService!!.delete(
                        resource,
                        object : IASSSolidNonRdfResourceCallback.Stub() {
                            override fun onResult(result: SolidNonRDFResource) {
                                try {
                                    cont.resume(
                                        SolidNetworkResponse.Success(
                                            reconstructNonRdf(
                                                result,
                                                resource.javaClass
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    cont.resume(SolidNetworkResponse.Exception(e))
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                                cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                            }
                        })
                }

                else -> cont.resume(
                    SolidNetworkResponse.Exception(
                        IllegalArgumentException("Resource must be SolidRDFResource or SolidNonRDFResource.")
                    )
                )
            }
        }
    }

    /**
     * Recursively deletes a container and all of its contents.
     * @param containerUri The URI of the LDP container to delete (must end with `/`).
     * @return [SolidNetworkResponse.Success] with `true` on success.
     */
    suspend fun deleteContainer(containerUri: URI): SolidNetworkResponse<Boolean> {
        checkBasicConditions()?.let {
            @Suppress("UNCHECKED_CAST")
            return it as SolidNetworkResponse<Boolean>
        }
        return suspendCancellableCoroutine { cont ->
            iASSResourceService!!.deleteContainer(
                containerUri.toString(),
                object : IASSUnitCallback.Stub() {
                    override fun onResult() {
                        cont.resume(SolidNetworkResponse.Success(true))
                    }

                    override fun onError(errorCode: Int, errorMessage: String) {
                        cont.resume(SolidNetworkResponse.Error(errorCode, errorMessage))
                    }
                }
            )
        }
    }
}
