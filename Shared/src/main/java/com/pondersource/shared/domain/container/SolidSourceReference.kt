package com.pondersource.shared.domain.container

import com.pondersource.shared.vocab.LDP

/**
 * A lightweight reference to a resource contained inside a [SolidContainer].
 * Populated from the container's RDF dataset, which Solid servers SHOULD enrich
 * with stat metadata per https://solidproject.org/TR/protocol (section on
 * container contained resource metadata).
 *
 * @property identifier  The IRI of the contained resource.
 * @property types       List of `rdf:type` IRI values for the resource.
 * @property size        File size in bytes (`stat:size`), if provided by the server.
 * @property modified    Last-modified datetime string (`dcterms:modified`), if provided.
 * @property mtime       Unix timestamp of last modification (`stat:mtime`), if provided.
 * @property contentType Content-type hint derived from `rdf:type` media-type URI, if present.
 */
data class SolidSourceReference(
    val identifier: String,
    val types: List<String>,
    val size: Long? = null,
    val modified: String? = null,
    val mtime: Long? = null,
    val contentType: String? = null,
) {
    fun isContainer(): Boolean =
        types.any { it == LDP.BASIC_CONTAINER ||
                    it == LDP.CONTAINER ||
                    it == LDP.DIRECT_CONTAINER ||
                    it == LDP.INDIRECT_CONTAINER }

    fun isContainerByUri(): Boolean = identifier.endsWith("/")
}