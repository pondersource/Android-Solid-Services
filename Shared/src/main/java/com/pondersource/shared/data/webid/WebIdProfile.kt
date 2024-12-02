package com.pondersource.shared.data.webid

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.Solid
import okhttp3.Headers
import java.net.URI

class WebIdProfile: RDFSource {

    private val privateTypeIndexKey = rdf.createIRI(Solid.privateTypeIndex)
    private val publicTypeIndexKey = rdf.createIRI(Solid.publicTypeIndex)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ): super(identifier, mediaType, dataset, headers)

    fun getPrivateTypeIndex(): String? {
        return dataset.defaultGraph.toList().find { it.predicate.equals(privateTypeIndexKey) }?.`object`?.value
    }

    fun setPrivateTypeIndex(webId: String, storage: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(webId),
                privateTypeIndexKey,
                rdf.createIRI("${storage}/settings/privateTypeIndex")
            )
        )
    }

    fun getPublicTypeIndex(): String? {
        return dataset.defaultGraph.toList().find { it.predicate.equals(publicTypeIndexKey) }?.`object`?.value
    }

    fun setPublicTypeIndex(webId: String, storage: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(webId),
                publicTypeIndexKey,
                rdf.createIRI("${storage}/settings/publicTypeIndex")
            )
        )
    }

}