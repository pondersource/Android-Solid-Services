package com.pondersource.shared.domain.util

import com.pondersource.shared.domain.access.WacAllow
import com.pondersource.shared.domain.network.HTTPHeaderName
import com.pondersource.shared.domain.network.HTTPLinkRelation
import com.pondersource.shared.vocab.PIM
import okhttp3.Headers
import java.net.URI

/**
 * Extension functions for parsing Solid-specific HTTP headers from OkHttp [Headers].
 * All functions return `null` / empty when the header is absent.
 *
 * Spec: https://solidproject.org/TR/protocol
 */

fun Headers.getContentLength(): Long {
    return this["content-length"]?.toLongOrNull() ?: -1L
}

/**
 * Returns the URI of the WAC / ACP access-control resource advertised by
 * `Link: <...>; rel="acl"`, or `null` if absent.
 */
fun Headers.getAclUri(): URI? =
    parseLinkRelation(this, HTTPLinkRelation.ACL)

/**
 * Returns the URI of the description resource advertised by
 * `Link: <...>; rel="describedby"`, or `null` if absent.
 */
fun Headers.getDescribedByUri(): URI? =
    parseLinkRelation(this, HTTPLinkRelation.DESCRIBED_BY)

/**
 * Returns the URI of the storage description resource advertised by
 * `Link: <...>; rel="http://www.w3.org/ns/solid/terms#storageDescription"`,
 * or `null` if absent.
 */
fun Headers.getStorageDescriptionUri(): URI? =
    parseLinkRelation(this, HTTPLinkRelation.STORAGE_DESCRIPTION)

/**
 * Returns the URI of the storage owner advertised by
 * `Link: <...>; rel="http://www.w3.org/ns/solid/terms#owner"`,
 * or `null` if absent.
 */
fun Headers.getOwnerUri(): URI? =
    parseLinkRelation(this, HTTPLinkRelation.OWNER)

/**
 * Returns `true` when the `Link` header contains
 * `rel="type" <http://www.w3.org/ns/pim/space#Storage>`,
 * indicating this resource is a Solid pod storage root.
 */
fun Headers.isStorage(): Boolean {
    return values(HTTPHeaderName.LINK).any { headerValue ->
        // The type value may appear as the full URI or a shortened form
        headerValue.contains(PIM.STORAGE_TYPE) &&
                headerValue.contains("""rel="${HTTPLinkRelation.TYPE}"""")
                    .or(headerValue.contains("rel=${HTTPLinkRelation.TYPE}"))
    }
}

/**
 * Returns the ETag value from the `ETag` response header, or `null` if absent.
 * Includes surrounding quotes if present (e.g. `"abc123"`).
 */
fun Headers.getETag(): String? = get(HTTPHeaderName.ETAG)

/**
 * Returns the `Last-Modified` header value, or `null` if absent.
 */
fun Headers.getLastModified(): String? = get(HTTPHeaderName.LAST_MODIFIED)

/**
 * Returns the `Location` header value as a [URI], or `null` if absent.
 * This is set on 201 Created responses.
 */
fun Headers.getLocation(): URI? =
    get(HTTPHeaderName.LOCATION)?.let { runCatching { URI.create(it) }.getOrNull() }

/**
 * Returns the set of HTTP methods listed in the `Allow` response header,
 * or an empty set if the header is absent.
 * Example: `Allow: GET, HEAD, OPTIONS, PUT, PATCH, DELETE`
 */
fun Headers.getAllowedMethods(): Set<String> =
    get(HTTPHeaderName.ALLOW)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: emptySet()

/**
 * Returns the parsed [WacAllow] from the `WAC-Allow` response header,
 * or `null` if the header is absent or malformed.
 */
fun Headers.getWacAllow(): WacAllow? =
    WacAllow.parse(get(HTTPHeaderName.WAC_ALLOW))

/**
 * Returns all `rel` values listed in `Link` headers as a flat list of strings.
 */
fun Headers.getLinkRelTypes(): List<String> {
    val results = mutableListOf<String>()
    values(HTTPHeaderName.LINK).forEach { headerValue ->
        val relRegex = Regex("""rel="?([^";,\s]+)"?""")
        relRegex.findAll(headerValue).forEach { results.add(it.groupValues[1]) }
    }
    return results
}

/**
 * Returns the set of type URIs advertised via `Link: <uri>; rel="type"` headers.
 * Used to identify resource types such as `ldp:BasicContainer` or `pim:Storage`.
 */
fun Headers.getLinkTypeUris(): Set<URI> {
    val results = mutableSetOf<URI>()
    values(HTTPHeaderName.LINK).forEach { headerValue ->
        headerValue.split(Regex(",(?=\\s*<)")).forEach { segment ->
            val relMatch = Regex("""rel="?([^";,\s]+)"?""").find(segment) ?: return@forEach
            if (relMatch.groupValues[1] == HTTPLinkRelation.TYPE) {
                val uriMatch = Regex("""<([^>]+)>""").find(segment) ?: return@forEach
                runCatching { URI.create(uriMatch.groupValues[1]) }.getOrNull()
                    ?.let { results.add(it) }
            }
        }
    }
    return results
}

private fun parseLinkRelation(headers: Headers, rel: String): URI? {
    headers.values(HTTPHeaderName.LINK).forEach { headerValue ->
        // Split on commas that are not inside angle brackets or quotes
        headerValue.split(Regex(",(?=\\s*<)")).forEach { segment ->
            val uriMatch = Regex("""<([^>]+)>""").find(segment) ?: return@forEach
            val relMatch = Regex("""rel="?([^";,\s]+)"?""").find(segment) ?: return@forEach
            if (relMatch.groupValues[1] == rel) {
                return runCatching { URI.create(uriMatch.groupValues[1]) }.getOrNull()
            }
        }
    }
    return null
}
