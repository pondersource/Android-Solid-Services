package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class ContactsIndexRDF: RDFSource {

    private val rdfKey = rdf.createIRI(RDF.type.toString())
    private val fullNameKey = rdf.createIRI(VCARD.fn)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)
}