package com.pondersource.shared.data.webid

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.PIM
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
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
            return buildJsonObject {
                put(KEY_IDENTIFIER, webId.getIdentifier().toString())
                put(KEY_TYPE, webId.getContentType())
                put(KEY_DATASET, webId.getEntity().toPlainString())
            }.toString()
        }

        fun readFromString(objectString: String): WebId {
            val obj = Json.parseToJsonElement(objectString).jsonObject
            return WebId(
                URI.create(obj[KEY_IDENTIFIER]!!.jsonPrimitive.content),
                MediaType.of(obj[KEY_TYPE]!!.jsonPrimitive.content),
                JsonLd.toRdf(JsonDocument.of(obj[KEY_DATASET]!!.jsonPrimitive.content.byteInputStream())).get(),
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