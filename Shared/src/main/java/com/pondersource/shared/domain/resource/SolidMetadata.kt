package com.pondersource.shared.domain.resource

import com.pondersource.shared.domain.access.WacAllow
import com.pondersource.shared.domain.util.getAclUri
import com.pondersource.shared.domain.util.getAllowedMethods
import com.pondersource.shared.domain.util.getETag
import com.pondersource.shared.domain.util.getLastModified
import com.pondersource.shared.domain.util.getLinkTypeUris
import com.pondersource.shared.domain.util.getLocation
import com.pondersource.shared.domain.util.getStorageDescriptionUri
import com.pondersource.shared.domain.util.getWacAllow
import okhttp3.Headers
import java.net.URI

/**
 * Solid-specific metadata extracted from HTTP response headers for a resource
 * retrieved from a Solid server.
 *
 * The fields in this class are mandated by various Solid Protocol specifications:
 * - [aclUri] — Solid Protocol §4.1.1 (Auxiliary Resources): `Link: rel="acl"`
 * - [storageDescriptionUri] — Solid Protocol §4.1.1: `Link: rel="storageDescription"`
 * - [wacAllow] — WAC spec §7: `WAC-Allow` header
 * - [allowedMethods] — HTTP spec + Solid Protocol: `Allow` header
 * - [linkTypes] — LDP + Solid Protocol: `Link: rel="type"` header
 * - [etag] — HTTP spec: `ETag` header
 *
 * The packaging of these fields into a single class is an implementation
 * convenience (not defined by any Solid spec), following the same pattern
 * used by the Inrupt Java Client SDK.
 */
data class SolidMetadata(
    /** URI of the WAC/ACP access-control resource. From `Link: rel="acl"`. */
    val aclUri: URI?,

    /** URI of the storage description resource. From `Link: rel="storageDescription"`. */
    val storageDescriptionUri: URI?,

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
) {
    companion object {
        fun from(headers: Headers): SolidMetadata = SolidMetadata(
            aclUri = headers.getAclUri(),
            storageDescriptionUri = headers.getStorageDescriptionUri(),
            wacAllow = headers.getWacAllow(),
            allowedMethods = headers.getAllowedMethods(),
            linkTypes = headers.getLinkTypeUris(),
            etag = headers.getETag(),
            lastModified = headers.getLastModified(),
            location = headers.getLocation(),
        )

        val EMPTY = SolidMetadata(
            aclUri = null,
            storageDescriptionUri = null,
            wacAllow= null,
            allowedMethods = emptySet(),
            linkTypes = emptySet(),
            etag = null,
            lastModified = null,
            location = null,
        )
    }
}