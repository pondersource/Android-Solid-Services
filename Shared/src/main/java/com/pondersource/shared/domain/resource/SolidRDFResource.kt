package com.pondersource.shared.domain.resource

import android.os.Parcel
import android.os.Parcelable
import com.apicatalog.jsonld.http.media.MediaType
import okhttp3.Headers
import java.net.URI

open class SolidRDFResource : RDFResource, SolidResource {

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SolidRDFResource> {
            override fun createFromParcel(parcel: Parcel): SolidRDFResource =
                SolidRDFResource(parcel)

            override fun newArray(size: Int): Array<SolidRDFResource?> = arrayOfNulls(size)
        }
    }

    private val metadata: SolidMetadata = SolidMetadata.from(getHeaders())

    protected constructor(inParcel: Parcel) : super(inParcel)

    constructor(identifier: URI) : this(identifier, null)

    constructor(identifier: URI, quads: List<RdfQuad>?) :
            this(identifier, quads, null)

    constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
            this(identifier, MediaType.JSON_LD, quads, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?,
    ) : super(identifier, mediaType, quads, headers)

    override fun getMetadata(): SolidMetadata = metadata

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
    }
}
