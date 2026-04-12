package com.pondersource.solidandroidclient.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.pondersource.shared.NonRDFSource
import com.pondersource.shared.RDFSource
import com.pondersource.shared.data.webid.WebId
import com.pondersource.shared.resource.Resource
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_CRUD_SERVICE
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.pondersource.solidandroidclient.IASSNonRdfResourceCallback
import com.pondersource.solidandroidclient.IASSRdfResourceCallback
import com.pondersource.solidandroidclient.IASSResourceService
import com.pondersource.solidandroidclient.sdk.SolidException.SolidResourceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Headers
import java.io.InputStream
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SolidResourceClient {

    companion object {
        @Volatile
        private var INSTANCE: SolidResourceClient? = null

        fun getInstance(
            context: Context,
            hasInstalledAndroidSolidServices: () -> Boolean
        ): SolidResourceClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SolidResourceClient(context, hasInstalledAndroidSolidServices).also { INSTANCE = it }
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

    fun resourceServiceConnectionState(): Flow<Boolean> = connectionFlow

    private fun checkBasicConditions() {
        if (!hasInstalledAndroidSolidServices()) throw SolidException.SolidAppNotFoundException()
        if (iASSResourceService == null) throw SolidException.SolidServiceConnectionException()
    }

    // ── Reconstruction helpers ────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun <T : Resource> reconstructRdf(source: RDFSource, clazz: Class<T>): T {
        if (clazz.isInstance(source)) return source as T
        return clazz.getConstructor(
            URI::class.java, MediaType::class.java, RdfDataset::class.java, Headers::class.java
        ).newInstance(
            source.getIdentifier(),
            MediaType.of(source.getContentType()),
            JsonLd.toRdf(JsonDocument.of(source.getEntity())).get(),
            source.getHeaders()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Resource> reconstructNonRdf(source: NonRDFSource, clazz: Class<T>): T {
        if (clazz.isInstance(source)) return source as T
        return clazz.getConstructor(
            URI::class.java, String::class.java, Headers::class.java, InputStream::class.java
        ).newInstance(
            source.getIdentifier(), source.getContentType(), source.getHeaders(), source.getEntity()
        )
    }

    // ── Public suspend API ────────────────────────────────────────────────────

    suspend fun getWebId(): WebId {
        checkBasicConditions()
        return suspendCoroutine { cont ->
            iASSResourceService!!.getWebId(object : IASSRdfResourceCallback.Stub() {
                override fun onResult(result: RDFSource) {
                    try { cont.resume(reconstructRdf(result, WebId::class.java)) }
                    catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                }
                override fun onError(errorCode: Int, errorMessage: String) {
                    cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                }
            })
        }
    }

    suspend fun <T : Resource> read(resourceUrl: String, clazz: Class<T>): T {
        checkBasicConditions()
        return suspendCoroutine { cont ->
            when {
                RDFSource::class.java.isAssignableFrom(clazz) -> {
                    iASSResourceService!!.readRdf(resourceUrl, object : IASSRdfResourceCallback.Stub() {
                        override fun onResult(result: RDFSource) {
                            try { cont.resume(reconstructRdf(result, clazz)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                NonRDFSource::class.java.isAssignableFrom(clazz) -> {
                    iASSResourceService!!.read(resourceUrl, object : IASSNonRdfResourceCallback.Stub() {
                        override fun onResult(result: NonRDFSource) {
                            try { cont.resume(reconstructNonRdf(result, clazz)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                else -> cont.resumeWithException(
                    SolidResourceException.NotSupportedClassException("Class must extend RDFSource or NonRDFSource.")
                )
            }
        }
    }

    suspend fun <T : Resource> create(resource: T): T {
        checkBasicConditions()
        return suspendCoroutine { cont ->
            when (resource) {
                is RDFSource -> {
                    iASSResourceService!!.createRdf(resource, object : IASSRdfResourceCallback.Stub() {
                        override fun onResult(result: RDFSource) {
                            try { cont.resume(reconstructRdf(result, resource.javaClass)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                is NonRDFSource -> {
                    iASSResourceService!!.create(resource, object : IASSNonRdfResourceCallback.Stub() {
                        override fun onResult(result: NonRDFSource) {
                            try { cont.resume(reconstructNonRdf(result, resource.javaClass)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                else -> cont.resumeWithException(
                    SolidResourceException.NotSupportedClassException("Resource must be RDFSource or NonRDFSource.")
                )
            }
        }
    }

    suspend fun <T : Resource> update(resource: T): T {
        checkBasicConditions()
        return suspendCoroutine { cont ->
            when (resource) {
                is RDFSource -> {
                    iASSResourceService!!.updateRdf(resource, object : IASSRdfResourceCallback.Stub() {
                        override fun onResult(result: RDFSource) {
                            try { cont.resume(reconstructRdf(result, resource.javaClass)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                is NonRDFSource -> {
                    iASSResourceService!!.update(resource, object : IASSNonRdfResourceCallback.Stub() {
                        override fun onResult(result: NonRDFSource) {
                            try { cont.resume(reconstructNonRdf(result, resource.javaClass)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                else -> cont.resumeWithException(
                    SolidResourceException.NotSupportedClassException("Resource must be RDFSource or NonRDFSource.")
                )
            }
        }
    }

    suspend fun <T : Resource> delete(resource: T): T {
        checkBasicConditions()
        return suspendCoroutine { cont ->
            when (resource) {
                is RDFSource -> {
                    iASSResourceService!!.deleteRdf(resource, object : IASSRdfResourceCallback.Stub() {
                        override fun onResult(result: RDFSource) {
                            try { cont.resume(reconstructRdf(result, resource.javaClass)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                is NonRDFSource -> {
                    iASSResourceService!!.delete(resource, object : IASSNonRdfResourceCallback.Stub() {
                        override fun onResult(result: NonRDFSource) {
                            try { cont.resume(reconstructNonRdf(result, resource.javaClass)) }
                            catch (e: Exception) { cont.resumeWithException(SolidResourceException.UnknownException(e.message ?: "")) }
                        }
                        override fun onError(errorCode: Int, errorMessage: String) {
                            cont.resumeWithException(handleSolidResourceException(errorCode, errorMessage))
                        }
                    })
                }
                else -> cont.resumeWithException(
                    SolidResourceException.NotSupportedClassException("Resource must be RDFSource or NonRDFSource.")
                )
            }
        }
    }
}
