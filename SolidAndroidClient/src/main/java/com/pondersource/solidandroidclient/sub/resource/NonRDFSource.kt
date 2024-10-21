package com.pondersource.solidandroidclient.sub.resource

import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI

open class NonRDFSource : Resource {

    private val identifier: URI
    private val contentType: String
    private val headers: Headers
    private val entity: InputStream

    constructor(
        identifier: URI,
        contentType: String,
        headers: Headers?,
        entity: InputStream
    ) {
        this.identifier = identifier
        this.contentType = contentType
        this.headers = headers ?: Headers.Builder().build()
        this.entity = entity
    }

    constructor(
        identifier: URI,
        contentType: String,
        entity: InputStream
    ) : this(identifier, contentType, null, entity)

    override fun getIdentifier(): URI {
        return identifier
    }

    override fun getContentType(): String {
        return contentType
    }

    override fun getHeaders(): Headers {
        return headers
    }

    override fun getEntity(): InputStream {
        return entity
    }

    override fun close() {
        try {
            getEntity().close()
        } catch (e: IOException) {
            throw UncheckedIOException("Unable to close NonRDFSource entity.", e)
        }
    }
}