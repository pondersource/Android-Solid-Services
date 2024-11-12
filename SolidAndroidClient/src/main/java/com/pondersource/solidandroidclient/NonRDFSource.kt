package com.pondersource.solidandroidclient

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pondersource.solidandroidclient.sub.resource.Resource
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

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<NonRDFSource> {
            override fun createFromParcel(parcel: Parcel): NonRDFSource {
                return NonRDFSource(parcel)
            }

            override fun newArray(size: Int): Array<NonRDFSource?> {
                return Array(size) { null }
            }
        }
    }


    private constructor(inParcel: Parcel) {
        this.identifier = URI.create(inParcel.readString())
        this.contentType = inParcel.readString()!!
        this.headers = Gson().fromJson<Headers>(inParcel.readString(), object : TypeToken<Headers>() {}.type)
        this.entity = inParcel.readString()!!.byteInputStream()
    }

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

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier.toString())
        dest.writeString(contentType)
        dest.writeString(Gson().toJson(headers))
        dest.writeString(getEntity().bufferedReader().use { it.readText() })
    }
}