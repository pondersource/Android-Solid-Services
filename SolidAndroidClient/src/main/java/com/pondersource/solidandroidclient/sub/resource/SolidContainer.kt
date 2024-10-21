package com.pondersource.solidandroidclient.sub.resource

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.LDP
import com.inrupt.client.vocabulary.RDF
import okhttp3.Headers
import java.net.URI

open class SolidContainer: SolidRDFSource {

    private val rdfType = rdf.createIRI(RDF.type.toString())
    private val contains = rdf.createIRI(LDP.contains.toString())

    constructor(
        identifier: URI
    ): this (identifier, null)

    constructor(
        identifier: URI,
        dataset: RdfDataset?
    ) : this(identifier, dataset, null)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?
    ) : this(identifier, mediaType, dataset, null)

    constructor(
        identifier: URI,
        dataset: RdfDataset?,
        headers: Headers?
    ) : this(identifier, MediaType.JSON_LD, dataset, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ) : super(identifier, mediaType, dataset, headers)

    fun getContained(): List<URI> {
        return dataset.defaultGraph.toList().filter {
            it.predicate.equals(contains) && it.subject.value == getIdentifier().toString()
        }.map { URI.create(it.`object`.value) }
    }
}