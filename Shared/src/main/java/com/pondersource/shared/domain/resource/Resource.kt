package com.pondersource.shared.domain.resource

import android.os.Parcelable
import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.net.URI

public interface Resource : AutoCloseable, Parcelable {

    public fun getIdentifier(): URI

    public fun getContentType(): String

    public fun getHeaders(): Headers

    @Throws(IOException::class)
    public fun getEntity(): InputStream

}