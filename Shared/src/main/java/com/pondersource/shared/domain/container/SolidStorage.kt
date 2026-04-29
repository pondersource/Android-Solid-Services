package com.pondersource.shared.domain.container

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.util.getOwnerUri
import com.pondersource.shared.domain.util.getStorageDescriptionUri
import com.pondersource.shared.vocab.PIM
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.Solid
import okhttp3.Headers
import java.net.URI

/**
 * Represents a Solid pod storage root container.
 *
 * A storage is an LDP BasicContainer that carries
 * `rdf:type pim:Storage` and is advertised via
 * `Link: rel="type" <http://www.w3.org/ns/pim/space#Storage>`.
 *
 * Spec: https://solidproject.org/TR/protocol — Storage
 */
class SolidStorage : SolidContainer {

    constructor(identifier: URI) : this(identifier, null, null)

    constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
            this(identifier, MediaType.JSON_LD, quads, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) : super(identifier, mediaType, quads, headers)

    fun getOwner(): URI? {
        val fromDataset = findPropertyForSubject(getIdentifier().toString(), Solid.OWNER)
            ?.let { runCatching { URI.create(it) }.getOrNull() }
        if (fromDataset != null) return fromDataset
        return getHeaders().getOwnerUri()
    }

    fun getStorageDescriptionUri(): URI? {
        val fromDataset =
            findPropertyForSubject(getIdentifier().toString(), Solid.STORAGE_DESCRIPTION)
                ?.let { runCatching { URI.create(it) }.getOrNull() }
        if (fromDataset != null) return fromDataset
        return getHeaders().getStorageDescriptionUri()
    }

    fun isStorageType(): Boolean =
        quads.any {
            it.subject == getIdentifier().toString() &&
                    it.predicate == RDF.TYPE &&
                    it.`object` == PIM.STORAGE_TYPE
        }
}