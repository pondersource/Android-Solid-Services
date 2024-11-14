package com.pondersource.solidandroidclient.sub.resource

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.LDP
import com.inrupt.client.vocabulary.RDF
import com.pondersource.solidandroidclient.vocab.PurlTerms
import com.pondersource.solidandroidclient.vocab.RDFSchema
import okhttp3.Headers
import java.net.URI

open class SolidContainer: SolidRDFSource {

    private val rdfType = rdf.createIRI(RDF.type.toString())
    private val contains = rdf.createIRI(LDP.contains.toString())
    private val label = rdf.createIRI(RDFSchema.label)
    private val created = rdf.createIRI(PurlTerms.created)

    private val containerRes = arrayListOf<SolidSourceReference>()

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
    ) : super(identifier, mediaType, dataset, headers) {

        dataset?.defaultGraph?.toList()?.filter {
            it.predicate.equals(contains) && it.subject.value == getIdentifier().toString()
        }?.forEach {
            val identifier = it.`object`
            val types = dataset.defaultGraph.toList().filter {
                it.subject.equals(identifier) && it.predicate.equals(rdfType)
            }.map { it.`object`.value }
            containerRes.add(SolidSourceReference(identifier.value, types))
        }
    }

    fun getContained(): List<SolidSourceReference> {
        return containerRes
    }

    fun hasLabel(): Boolean {
        return getLabel() != null
    }

    fun getLabel(): String? {
        val value = dataset.defaultGraph.toList().find {
            it.subject.value == getIdentifier().toString() && it.predicate.equals(label)
        }
        return value?.`object`?.value
    }
}