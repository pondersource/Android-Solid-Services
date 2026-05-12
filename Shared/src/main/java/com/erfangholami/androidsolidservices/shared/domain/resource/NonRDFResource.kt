package com.erfangholami.androidsolidservices.shared.domain.resource

import android.os.Parcel
import android.os.Parcelable
import com.erfangholami.androidsolidservices.shared.domain.util.encodeUri
import com.erfangholami.androidsolidservices.shared.domain.util.encodeUriString
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI

public open class NonRDFResource : Resource {

    private val identifier: URI
    private val contentType: String
    private val headers: Headers
    private val entity: InputStream

    public companion object {
        @JvmField
        public val CREATOR: Parcelable.Creator<NonRDFResource> = object : Parcelable.Creator<NonRDFResource> {
            override fun createFromParcel(parcel: Parcel): NonRDFResource {
                return NonRDFResource(parcel)
            }

            override fun newArray(size: Int): Array<NonRDFResource?> {
                return Array(size) { null }
            }
        }
    }


    protected constructor(inParcel: Parcel) {
        this.identifier = encodeUriString(inParcel.readString()!!)
        this.contentType = inParcel.readString()!!
        val headersMap = Json.decodeFromString<Map<String, List<String>>>(inParcel.readString()!!)
        this.headers = Headers.Builder()
            .apply { headersMap.forEach { (name, values) -> values.forEach { add(name, it) } } }
            .build()
        this.entity = inParcel.readString()!!.byteInputStream()
    }

    public constructor(
        identifier: URI,
        contentType: String,
        headers: Headers?,
        entity: InputStream
    ) {
        this.identifier = encodeUri(identifier)
        this.contentType = contentType
        this.headers = headers ?: Headers.Builder().build()
        this.entity = entity
    }

    public constructor(
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

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier.toString())
        dest.writeString(contentType)
        dest.writeString(Json.encodeToString(headers.toMultimap()))
        dest.writeString(getEntity().bufferedReader().use { it.readText() })
    }
}