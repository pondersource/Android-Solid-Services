package com.pondersource.shared.data

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.inrupt.client.vocabulary.PIM
import com.inrupt.client.vocabulary.RDF
import com.inrupt.client.vocabulary.RDFS
import com.pondersource.shared.RDFSource
import com.pondersource.shared.util.toPlainString
import com.pondersource.shared.vocab.Solid
import okhttp3.Headers
import java.net.URI

class WebId: RDFSource {

    companion object {

        private const val KEY_IDENTIFIER = "identifier"
        private const val KEY_TYPE = "type"
        private const val KEY_DATASET = "dataset"

        fun writeToString(webId: WebId?): String? {
            if (webId == null) {
                return null
            }
            return JsonObject().apply {
                addProperty(KEY_IDENTIFIER, webId.getIdentifier().toString())
                addProperty(KEY_TYPE, webId.getContentType())
                addProperty(KEY_DATASET, webId.getEntity().toPlainString())
            }.toString()
        }

        fun readFromString(objectString: String): WebId {
            val obj = JsonParser.parseString(objectString).asJsonObject
            return WebId(
                URI.create(obj.get(KEY_IDENTIFIER).asString),
                MediaType.of(obj.get(KEY_TYPE).asString),
                JsonLd.toRdf(JsonDocument.of(obj.get(KEY_DATASET).asString.byteInputStream())).get(),
            )
        }
    }

    private val rdfType = rdf.createIRI(RDF.type.toString())
    private val oidcIssuer = rdf.createIRI(com.inrupt.client.vocabulary.Solid.oidcIssuer.toString())
    private val seeAlso = rdf.createIRI(RDFS.seeAlso.toString())
    private val storage = rdf.createIRI(PIM.storage.toString())
    private val privateTypeIndexKey = rdf.createIRI(Solid.privateTypeIndex)
    private val publicTypeIndexKey = rdf.createIRI(Solid.publicTypeIndex)
    private val primaryTopicKey = rdf.createIRI("http://xmlns.com/foaf/0.1/isPrimaryTopicOf")

    constructor(
        identifier: URI,
        dataset: RdfDataset,
    ): super(identifier, dataset)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset,
    ): super(identifier, mediaType, dataset)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset,
        headers: Headers?
    ): super(identifier, mediaType, dataset, headers)

    fun getTypes(): List<URI> {
        return findAllProperties(rdfType)
            .map { URI.create(it.value) }
    }

    fun getOidcIssuers(): List<URI> {
        return findAllProperties(oidcIssuer)
            .map { URI.create(it.value) }
    }

    fun getRelatedResources(): List<URI> {
        return findAllProperties(seeAlso)
            .map { URI.create(it.value) }
    }

    fun getStorages(): List<URI> {
        return findAllProperties(storage)
            .map { URI.create(it.value) }
    }

    fun getPrivateTypeIndex(): String? {
        return dataset.defaultGraph.toList().find { it.predicate.equals(privateTypeIndexKey) }?.`object`?.value
    }

    fun getPublicTypeIndex(): String? {
        return dataset.defaultGraph.toList().find { it.predicate.equals(publicTypeIndexKey) }?.`object`?.value
    }

    fun getProfileUrl(): String {
        return dataset.defaultGraph.toList().find { it.predicate.equals(primaryTopicKey) }!!.`object`!!.value
    }
}