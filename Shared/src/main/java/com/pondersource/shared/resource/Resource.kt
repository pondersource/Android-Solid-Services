package com.pondersource.shared.resource

import android.os.Parcelable
import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.net.URI

interface Resource : AutoCloseable, Parcelable {

    fun getIdentifier(): URI

    fun getContentType(): String

    fun getHeaders(): Headers

    @Throws(IOException::class)
    fun getEntity(): InputStream

}