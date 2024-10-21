package com.pondersource.solidandroidclient.sub.resource

import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.net.URI

interface Resource : AutoCloseable {

    fun getIdentifier(): URI

    fun getContentType(): String

    fun getHeaders(): Headers

    @Throws(IOException::class)
    fun getEntity(): InputStream

}