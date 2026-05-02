package com.pondersource.shared.domain.resource

import android.os.Parcel
import android.os.Parcelable
import com.apicatalog.jsonld.http.media.MediaType
import okhttp3.Headers
import java.net.URI

public open class SolidRDFResource : RDFResource, SolidResource {

    public companion object {
        @JvmField
        public val CREATOR: Parcelable.Creator<SolidRDFResource> = object : Parcelable.Creator<SolidRDFResource> {
            override fun createFromParcel(parcel: Parcel): SolidRDFResource =
                SolidRDFResource(parcel)

            override fun newArray(size: Int): Array<SolidRDFResource?> = arrayOfNulls(size)
        }
    }

    private val metadata: SolidMetadata = SolidMetadata.from(getHeaders())

    protected constructor(inParcel: Parcel) : super(inParcel)

    public constructor(identifier: URI) : this(identifier, null)

    public constructor(identifier: URI, quads: List<RdfQuad>?) :
            this(identifier, quads, null)

    public constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
            this(identifier, MediaType.JSON_LD, quads, headers)

    public constructor(
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
