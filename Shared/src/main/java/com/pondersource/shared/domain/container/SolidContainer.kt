package com.pondersource.shared.domain.container

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.domain.container.SolidSourceReference
import com.pondersource.shared.vocab.DC
import com.pondersource.shared.vocab.LDP
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.RDFS
import com.pondersource.shared.vocab.STAT
import okhttp3.Headers
import java.net.URI

/**
 * Represents an LDP BasicContainer resource.
 * Parses contained resource references and their optional server-supplied
 * metadata (size, modified, mtime) from the quad store.
 *
 * Spec: https://solidproject.org/TR/protocol — Container resources
 */
open class SolidContainer : SolidRDFResource {

    private val containerRes = arrayListOf<SolidSourceReference>()

    constructor(identifier: URI) : this(identifier, null)

    constructor(identifier: URI, quads: List<RdfQuad>?) :
        this(identifier, quads, null)

    constructor(identifier: URI, mediaType: MediaType, quads: List<RdfQuad>?) :
        this(identifier, mediaType, quads, null)

    constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
        this(identifier, MediaType.JSON_LD, quads, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) : super(identifier, mediaType, quads, headers) {
        parseContainedResources()
    }

    private fun parseContainedResources() {
        val containerUri = getIdentifier().toString()
        quads
            .filter { it.predicate == LDP.CONTAINS && it.subject == containerUri }
            .forEach { containsQuad ->
                val containedIri = containsQuad.`object`

                val types = quads
                    .filter { it.subject == containedIri && it.predicate == RDF.TYPE }
                    .map { it.`object` }

                val size = quads
                    .find { it.subject == containedIri && it.predicate == STAT.SIZE }
                    ?.`object`?.toLongOrNull()

                val modified = quads
                    .find { it.subject == containedIri && it.predicate == DC.MODIFIED }
                    ?.`object`

                val mtime = quads
                    .find { it.subject == containedIri && it.predicate == STAT.MTIME }
                    ?.`object`?.toLongOrNull()

                containerRes.add(
                    SolidSourceReference(
                        identifier = containedIri,
                        types = types,
                        size = size,
                        modified = modified,
                        mtime = mtime,
                    )
                )
            }
    }

    fun getContained(): List<SolidSourceReference> = containerRes

    fun hasLabel(): Boolean = getLabel() != null

    fun getLabel(): String? =
        quads.find {
            it.subject == getIdentifier().toString() && it.predicate == RDFS.LABEL
        }?.`object`
}