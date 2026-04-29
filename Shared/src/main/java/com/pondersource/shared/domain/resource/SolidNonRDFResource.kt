package com.pondersource.shared.domain.resource

import android.os.Parcel
import android.os.Parcelable
import okhttp3.Headers
import java.io.InputStream
import java.net.URI

open class SolidNonRDFResource : NonRDFResource, SolidResource {

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SolidNonRDFResource> {
            override fun createFromParcel(parcel: Parcel): SolidNonRDFResource =
                SolidNonRDFResource(parcel)

            override fun newArray(size: Int): Array<SolidNonRDFResource?> = arrayOfNulls(size)
        }
    }

    private val metadata: SolidMetadata = SolidMetadata.from(getHeaders())

    protected constructor(inParcel: Parcel) : super(inParcel)

    constructor(
        identifier: URI,
        contentType: String,
        entity: InputStream,
    ) : this(identifier, contentType, entity, null)

    constructor(
        identifier: URI,
        contentType: String,
        entity: InputStream,
        headers: Headers?,
    ) : super(identifier, contentType, headers, entity)

    override fun getMetadata(): SolidMetadata = metadata

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
    }
}
