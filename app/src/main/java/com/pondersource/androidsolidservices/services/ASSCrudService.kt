package com.pondersource.androidsolidservices.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pondersource.androidsolidservices.usecase.Authenticator
import com.pondersource.androidsolidservices.usecase.SolidResourceManager
import com.pondersource.solidandroidclient.IASSCrudService
import com.pondersource.solidandroidclient.sub.resource.NonRDFSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.Blocking
import java.net.URI
import javax.inject.Inject

@AndroidEntryPoint
class ASSCrudService: Service() {

    @Inject
    lateinit var authenticator : Authenticator
    @Inject
    lateinit var solidResourceManager : SolidResourceManager

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    private val binder = object : IASSCrudService.Stub() {
        @Blocking
        override fun get(resourceUrl: String?): String {
            return runBlocking {
                val result = solidResourceManager.read(URI.create(resourceUrl), NonRDFSource::class.java)
                return@runBlocking ""
            }

        }
    }
}