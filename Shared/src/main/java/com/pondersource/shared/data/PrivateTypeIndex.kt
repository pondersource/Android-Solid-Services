package com.pondersource.shared.data

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.pondersource.shared.vocab.Solid
import okhttp3.Headers
import java.net.URI

class PrivateTypeIndex: SettingTypeIndex {

    private val UnlistedDocument = rdf.createIRI(Solid.UnlistedDocument)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ): super(identifier, mediaType, dataset, headers)

    override fun setTypes() {
        val types = dataset.defaultGraph.toList().filter {
            it.subject.value == getIdentifier().toString() && it.predicate.equals(typeKey)
        }

        if(types.isEmpty()) {
            addTriple(
                rdf.createTriple(
                    rdf.createIRI(getIdentifier().toString()),
                    typeKey,
                    TypeIndex
                ),
                Int.MAX_VALUE
            )
            addTriple(
                rdf.createTriple(
                    rdf.createIRI(getIdentifier().toString()),
                    typeKey,
                    UnlistedDocument
                ),
                Int.MAX_VALUE
            )
        }
    }
}