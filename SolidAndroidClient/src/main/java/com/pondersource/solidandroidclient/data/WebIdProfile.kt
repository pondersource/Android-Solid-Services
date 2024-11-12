package com.pondersource.solidandroidclient.data

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.inrupt.client.vocabulary.PIM
import com.inrupt.client.vocabulary.RDF
import com.inrupt.client.vocabulary.RDFS
import com.inrupt.client.vocabulary.Solid
import com.pondersource.solidandroidclient.RDFSource
import com.pondersource.solidandroidclient.util.toPlainString
import java.net.URI

class WebIdProfile: RDFSource {

    companion object {

        private const val KEY_IDENTIFIER = "identifier"
        private const val KEY_TYPE = "type"
        private const val KEY_DATASET = "dataset"

        fun writeToString(webIdProfile: WebIdProfile?): String? {
            if (webIdProfile ==  null) {
                return null
            }
            return JsonObject().apply {
                addProperty(KEY_IDENTIFIER, webIdProfile.getIdentifier().toString())
                addProperty(KEY_TYPE, webIdProfile.getContentType())
                addProperty(KEY_DATASET, webIdProfile.getEntity().toPlainString())
            }.toString()
        }

        fun readFromString(objectString: String): WebIdProfile {
            val obj = JsonParser.parseString(objectString).asJsonObject
            return WebIdProfile(
                URI.create(obj.get(KEY_IDENTIFIER).asString),
                MediaType.of(obj.get(KEY_TYPE).asString),
                JsonLd.toRdf(JsonDocument.of(obj.get(KEY_DATASET).asString.byteInputStream())).get(),
            )
        }
    }

    private val rdfType = rdf.createIRI(RDF.type.toString())
    private val oidcIssuer = rdf.createIRI(Solid.oidcIssuer.toString())
    private val seeAlso = rdf.createIRI(RDFS.seeAlso.toString())
    private val storage = rdf.createIRI(PIM.storage.toString())

    constructor(
        identifier: URI,
        dataset: RdfDataset,
    ): super(identifier, dataset)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset,
    ): super(identifier, mediaType, dataset)

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
}