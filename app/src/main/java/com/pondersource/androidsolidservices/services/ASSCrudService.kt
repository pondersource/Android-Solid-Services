package com.pondersource.androidsolidservices.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pondersource.androidsolidservices.base.Authenticator
import com.pondersource.androidsolidservices.base.CRUD
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
    lateinit var crud : CRUD

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    private val binder = object : IASSCrudService.Stub() {
        @Blocking
        override fun get(resourceUrl: String?): String {
            return runBlocking {
                val result = crud.read(URI.create(resourceUrl), NonRDFSource::class.java)
                return@runBlocking ""
            }

        }
    }
}