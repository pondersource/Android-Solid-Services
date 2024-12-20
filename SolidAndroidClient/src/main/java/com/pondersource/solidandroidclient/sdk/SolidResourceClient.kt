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

class SolidResourceClient {

    companion object {
        @Volatile
        private lateinit var INSTANCE: SolidResourceClient

        fun getInstance(
            context: Context,
            hasInstalledAndroidSolidServices: () -> Boolean
        ): SolidResourceClient {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidResourceClient(context, hasInstalledAndroidSolidServices)
                INSTANCE
            }
        }
    }

    private var iASSAuthService: IASSResourceService? = null
    private val connectionFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val hasInstalledAndroidSolidServices: () -> Boolean
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSAuthService = IASSResourceService.Stub.asInterface(service)
            connectionFlow.value = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSAuthService = null
            connectionFlow.value = false
        }
    }

    private constructor(
        context: Context,
        hasInstalledAndroidSolidServices: () -> Boolean
    ) {
        this.hasInstalledAndroidSolidServices = hasInstalledAndroidSolidServices
        val intent = Intent().apply {
            setClassName(
                ANDROID_SOLID_SERVICES_PACKAGE_NAME,
                ANDROID_SOLID_SERVICES_CRUD_SERVICE
            )
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun checkIfAppIsInstalled() {
        if (!hasInstalledAndroidSolidServices()) {
            throw SolidException.SolidAppNotFoundException()
        }
    }

    private fun checkServiceConnection() {
        if (iASSAuthService == null) {
            throw SolidException.SolidServiceConnectionException()
        }
    }

    fun resourceServiceConnectionState(): Flow<Boolean> {
        return connectionFlow
    }

    private fun checkBasicConditions() {
        checkIfAppIsInstalled()
        checkServiceConnection()
    }

    fun getWebId(
        callback: SolidResourceCallback<WebId>
    ) {
        checkBasicConditions()

        iASSAuthService!!.getWebId(object: IASSRdfResourceCallback.Stub() {
            override fun onResult(webIdResult: RDFSource) {
                val returnValue = WebId::class.java
                    .getConstructor(
                        URI::class.java,
                        MediaType::class.java,
                        RdfDataset::class.java,
                    )
                    .newInstance(
                        webIdResult.getIdentifier(),
                        MediaType.of(webIdResult.getContentType()),
                        JsonLd.toRdf(JsonDocument.of(webIdResult.getEntity())).get(),
                    )
                callback.onResult(returnValue)
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                callback.onError(handleSolidResourceException(errorCode, errorMessage))
            }
        })
    }

    fun <T: Resource> create(
        resource: T,
        callback: SolidResourceCallback<T>
    ) {
        checkBasicConditions()

        if (resource is RDFSource) {
            iASSAuthService!!.createRdf(resource, object: IASSRdfResourceCallback.Stub() {
                override fun onResult(result: RDFSource) {
                    if (resource::class.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = resource::class.java
                            .getConstructor(
                                URI::class.java,
                                MediaType::class.java,
                                RdfDataset::class.java,
                                Headers::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                MediaType.of(result.getContentType()),
                                JsonLd.toRdf(JsonDocument.of(result.getEntity())).get(),
                                result.getHeaders()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }

            })
        } else if (resource is NonRDFSource) {
            iASSAuthService!!.create(resource, object: IASSNonRdfResourceCallback.Stub() {
                override fun onResult(result: NonRDFSource) {
                    if (resource::class.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = resource::class.java
                            .getConstructor(
                                URI::class.java,
                                String::class.java,
                                Headers::class.java,
                                InputStream::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                result.getContentType(),
                                result.getHeaders(),
                                result.getEntity()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }
            })
        } else {
            throw SolidResourceException.NotSupportedClassException("Objects which are RDFSource or NonRDFSource or inherited from them can be created.")
        }
    }

    fun <T: Resource> read(
        resourceUrl: String,
        clazz: Class<T>,
        callback: SolidResourceCallback<T>
    ) {
        checkBasicConditions()

        if (RDFSource::class.java.isAssignableFrom(clazz)) {
            iASSAuthService!!.readRdf(resourceUrl, object: IASSRdfResourceCallback.Stub() {
                override fun onResult(result: RDFSource) {
                    if (clazz.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = clazz
                            .getConstructor(
                                URI::class.java,
                                MediaType::class.java,
                                RdfDataset::class.java,
                                Headers::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                MediaType.of(result.getContentType()),
                                JsonLd.toRdf(JsonDocument.of(result.getEntity())).get(),
                                result.getHeaders()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }

            })
        } else if (NonRDFSource::class.java.isAssignableFrom(clazz)) {
            iASSAuthService!!.read(resourceUrl, object: IASSNonRdfResourceCallback.Stub() {
                override fun onResult(result: NonRDFSource) {
                    if (clazz.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = clazz
                            .getConstructor(
                                URI::class.java,
                                String::class.java,
                                Headers::class.java,
                                InputStream::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                result.getContentType(),
                                result.getHeaders(),
                                result.getEntity()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }
            })
        } else {
            throw SolidResourceException.NotSupportedClassException("Objects which are RDFSource or NonRDFSource or inherited from them can be read.")
        }
    }

    fun <T: Resource> update(
        resource: T,
        callback: SolidResourceCallback<T>
    ) {
        checkBasicConditions()

        if (resource is RDFSource) {
            iASSAuthService!!.updateRdf(resource, object: IASSRdfResourceCallback.Stub() {
                override fun onResult(result: RDFSource) {
                    if (resource::class.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = resource::class.java
                            .getConstructor(
                                URI::class.java,
                                MediaType::class.java,
                                RdfDataset::class.java,
                                Headers::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                MediaType.of(result.getContentType()),
                                JsonLd.toRdf(JsonDocument.of(result.getEntity())).get(),
                                result.getHeaders()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }

            })
        } else if (resource is NonRDFSource) {
            iASSAuthService!!.update(resource, object: IASSNonRdfResourceCallback.Stub() {
                override fun onResult(result: NonRDFSource) {
                    if (resource::class.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = resource::class.java
                            .getConstructor(
                                URI::class.java,
                                String::class.java,
                                Headers::class.java,
                                InputStream::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                result.getContentType(),
                                result.getHeaders(),
                                result.getEntity()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }
            })
        } else {
            throw SolidResourceException.NotSupportedClassException("Objects which are RDFSource or NonRDFSource or inherited from them can be updated.")
        }
    }

    fun <T: Resource> delete(
        resource: T,
        callback: SolidResourceCallback<T>
    ) {
        checkBasicConditions()

        if (resource is RDFSource) {
            iASSAuthService!!.deleteRdf(resource, object: IASSRdfResourceCallback.Stub() {
                override fun onResult(result: RDFSource) {
                    if (resource::class.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = resource::class.java
                            .getConstructor(
                                URI::class.java,
                                MediaType::class.java,
                                RdfDataset::class.java,
                                Headers::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                MediaType.of(result.getContentType()),
                                JsonLd.toRdf(JsonDocument.of(result.getEntity())).get(),
                                result.getHeaders()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }

            })
        } else if (resource is NonRDFSource) {
            iASSAuthService!!.delete(resource, object: IASSNonRdfResourceCallback.Stub() {
                override fun onResult(result: NonRDFSource) {
                    if (resource::class.isInstance(result)) {
                        callback.onResult(result as T)
                    } else {
                        val returnValue = resource::class.java
                            .getConstructor(
                                URI::class.java,
                                String::class.java,
                                Headers::class.java,
                                InputStream::class.java
                            )
                            .newInstance(
                                result.getIdentifier(),
                                result.getContentType(),
                                result.getHeaders(),
                                result.getEntity()
                            )
                        callback.onResult(returnValue)
                    }
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    callback.onError(handleSolidResourceException(errorCode, errorMessage))
                }
            })
        } else {
            throw SolidResourceException.NotSupportedClassException("Objects which are RDFSource or NonRDFSource or inherited from them can be updated.")
        }
    }
}