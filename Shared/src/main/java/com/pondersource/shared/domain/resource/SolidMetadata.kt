package com.pondersource.shared.domain.resource

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.pondersource.shared.domain.access.WacAllow
import com.pondersource.shared.domain.util.getAcceptPatch
import com.pondersource.shared.domain.util.getAcceptPost
import com.pondersource.shared.domain.util.getAcceptPut
import com.pondersource.shared.domain.util.getAclUri
import com.pondersource.shared.domain.util.getAllowedMethods
import com.pondersource.shared.domain.util.getContentLength
import com.pondersource.shared.domain.util.getContentType
import com.pondersource.shared.domain.util.getDescribedByUri
import com.pondersource.shared.domain.util.getETag
import com.pondersource.shared.domain.util.getLastModified
import com.pondersource.shared.domain.util.getLinkTypeUris
import com.pondersource.shared.domain.util.getLocation
import com.pondersource.shared.domain.util.getOidcIssuerUri
import com.pondersource.shared.domain.util.getOwnerUri
import com.pondersource.shared.domain.util.getStorageDescriptionUri
import com.pondersource.shared.domain.util.getWacAllow
import com.pondersource.shared.domain.util.getWwwAuthenticate
import com.pondersource.shared.domain.util.isStorage
import okhttp3.Headers
import java.net.URI

/**
 * Solid-specific metadata extracted from HTTP response headers for a resource
 * retrieved from a Solid server.
 *
 * The fields in this class are mandated by various Solid Protocol specifications:
 * - [aclUri] — Solid Protocol §4.1.1 (Auxiliary Resources): `Link: rel="acl"`
 * - [storageDescriptionUri] — Solid Protocol §4.1.1: `Link: rel="storageDescription"`
 * - [ownerUri] — Solid Protocol §4.1.1: `Link: rel="solid:owner"`
 * - [wacAllow] — WAC spec §7: `WAC-Allow` header
 * - [allowedMethods] — HTTP spec + Solid Protocol: `Allow` header
 * - [linkTypes] — LDP + Solid Protocol: `Link: rel="type"` header
 * - [etag] — HTTP spec: `ETag` header
 * - [oidcIssuerUri] — Solid-OIDC: `Link: rel="solid:oidcIssuer"`
 * - [isStorage] — Solid Protocol: derived from `Link: rel="type" <pim:Storage>`
 *
 * The packaging of these fields into a single class is an implementation
 * convenience (not defined by any Solid spec), following the same pattern
 * used by the Inrupt Java Client SDK.
 */
public data class SolidMetadata(
    /** URI of the WAC/ACP access-control resource. From `Link: rel="acl"`. */
    val aclUri: URI?,

    /** URI of the storage description resource. From `Link: rel="storageDescription"`. */
    val storageDescriptionUri: URI?,

    /** URI of the storage owner. From `Link: rel="solid:owner"`. */
    val ownerUri: URI?,

    /** Parsed `WAC-Allow` header — what modes the current caller has on this resource. */
    val wacAllow: WacAllow?,

    /** HTTP methods the server accepts for this resource. From `Allow` header. */
    val allowedMethods: Set<String>,

    /**
     * RDF type IRIs advertised via `Link: <iri>; rel="type"` headers.
     * Common values: `ldp:Resource`, `ldp:BasicContainer`, `pim:Storage`.
     */
    val linkTypes: Set<URI>,

    /** ETag for cache validation and conditional requests. From `ETag` header. */
    val etag: String?,

    /** Last-Modified timestamp string. From `Last-Modified` header. */
    val lastModified: String?,

    /** Location URI set on 201 Created responses. From `Location` header. */
    val location: URI?,

    /** Media type of the resource. From `Content-Type` header. */
    val contentType: String?,

    /** Size of the resource body in bytes. From `Content-Length` header. -1 if absent. */
    val contentLength: Long,

    /** URI of the resource that describes this resource. From `Link: rel="describedby"`. */
    val describeByUri: URI?,

    /** Media types accepted for PATCH requests. From `Accept-Patch` header. */
    val acceptPatch: List<String>,

    /** Media types accepted for POST requests. From `Accept-Post` header. */
    val acceptPost: List<String>,

    /** Media types accepted for PUT requests. From `Accept-Put` header. */
    val acceptPut: List<String>,

    /** WWW-Authenticate challenge. From `WWW-Authenticate` header. Present on 401 responses. */
    val wwwAuthenticate: String?,

    /**
     * OIDC issuer URI for this resource or WebID.
     * From `Link: <iri>; rel="http://www.w3.org/ns/solid/terms#oidcIssuer"`.
     * Spec: https://solidproject.org/TR/oidc
     */
    val oidcIssuerUri: URI?,

    /**
     * Whether this resource is a Solid pod storage root.
     * True when `Link: rel="type" <http://www.w3.org/ns/pim/space#Storage>` is present.
     */
    val isStorage: Boolean,
) : Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(aclUri?.toString())
        dest.writeString(storageDescriptionUri?.toString())
        dest.writeString(ownerUri?.toString())
        dest.writeParcelable(wacAllow, flags)
        dest.writeStringList(allowedMethods.toList())
        dest.writeStringList(linkTypes.map { it.toString() })
        dest.writeString(etag)
        dest.writeString(lastModified)
        dest.writeString(location?.toString())
        dest.writeString(contentType)
        dest.writeLong(contentLength)
        dest.writeString(describeByUri?.toString())
        dest.writeStringList(acceptPatch)
        dest.writeStringList(acceptPost)
        dest.writeStringList(acceptPut)
        dest.writeString(wwwAuthenticate)
        dest.writeString(oidcIssuerUri?.toString())
        dest.writeByte(if (isStorage) 1 else 0)
    }

    public companion object {
        @JvmField
        public val CREATOR: Parcelable.Creator<SolidMetadata> = object : Parcelable.Creator<SolidMetadata> {
            override fun createFromParcel(parcel: Parcel): SolidMetadata = SolidMetadata(
                aclUri = parcel.readString()?.let { runCatching { URI.create(it) }.getOrNull() },
                storageDescriptionUri = parcel.readString()
                    ?.let { runCatching { URI.create(it) }.getOrNull() },
                ownerUri = parcel.readString()?.let { runCatching { URI.create(it) }.getOrNull() },
                wacAllow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    parcel.readParcelable(WacAllow::class.java.classLoader, WacAllow::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    parcel.readParcelable(WacAllow::class.java.classLoader)
                },
                allowedMethods = parcel.createStringArrayList()!!.toSet(),
                linkTypes = parcel.createStringArrayList()!!
                    .mapNotNull { runCatching { URI.create(it) }.getOrNull() }
                    .toSet(),
                etag = parcel.readString(),
                lastModified = parcel.readString(),
                location = parcel.readString()?.let { runCatching { URI.create(it) }.getOrNull() },
                contentType = parcel.readString(),
                contentLength = parcel.readLong(),
                describeByUri = parcel.readString()
                    ?.let { runCatching { URI.create(it) }.getOrNull() },
                acceptPatch = parcel.createStringArrayList()!!,
                acceptPost = parcel.createStringArrayList()!!,
                acceptPut = parcel.createStringArrayList()!!,
                wwwAuthenticate = parcel.readString(),
                oidcIssuerUri = parcel.readString()
                    ?.let { runCatching { URI.create(it) }.getOrNull() },
                isStorage = parcel.readByte() != 0.toByte(),
            )

            override fun newArray(size: Int): Array<SolidMetadata?> = arrayOfNulls(size)
        }

        public fun from(headers: Headers): SolidMetadata = SolidMetadata(
            aclUri = headers.getAclUri(),
            storageDescriptionUri = headers.getStorageDescriptionUri(),
            ownerUri = headers.getOwnerUri(),
            wacAllow = headers.getWacAllow(),
            allowedMethods = headers.getAllowedMethods(),
            linkTypes = headers.getLinkTypeUris(),
            etag = headers.getETag(),
            lastModified = headers.getLastModified(),
            location = headers.getLocation(),
            contentType = headers.getContentType(),
            contentLength = headers.getContentLength(),
            describeByUri = headers.getDescribedByUri(),
            acceptPatch = headers.getAcceptPatch(),
            acceptPost = headers.getAcceptPost(),
            acceptPut = headers.getAcceptPut(),
            wwwAuthenticate = headers.getWwwAuthenticate(),
            oidcIssuerUri = headers.getOidcIssuerUri(),
            isStorage = headers.isStorage(),
        )

        public val EMPTY: SolidMetadata = SolidMetadata(
            aclUri = null,
            storageDescriptionUri = null,
            ownerUri = null,
            wacAllow = null,
            allowedMethods = emptySet(),
            linkTypes = emptySet(),
            etag = null,
            lastModified = null,
            location = null,
            contentType = null,
            contentLength = -1L,
            describeByUri = null,
            acceptPatch = emptyList(),
            acceptPost = emptyList(),
            acceptPut = emptyList(),
            wwwAuthenticate = null,
            oidcIssuerUri = null,
            isStorage = false,
        )
    }
}
