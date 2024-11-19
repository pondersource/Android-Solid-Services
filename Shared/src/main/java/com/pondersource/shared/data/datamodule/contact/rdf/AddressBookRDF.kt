package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.DC
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class AddressBookRDF: RDFSource {

    private val titleKey = rdf.createIRI(DC.title)
    private val nameEmailIndexKey = rdf.createIRI(VCARD.nameEmailIndex)
    private val groupIndexKey = rdf.createIRI(VCARD.groupIndex)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)

    fun getTitle(): String {
        return dataset.defaultGraph.toList().find {
            it.predicate.equals(titleKey)
        }!!.`object`.value
    }

    fun getNameEmailIndex() : String {
        return dataset.defaultGraph.toList().find {
            it.predicate.equals(nameEmailIndexKey)
        }!!.`object`.value
    }

    fun getGroupIndex() : String {
        return dataset.defaultGraph.toList().find {
            it.predicate.equals(groupIndexKey)
        }!!.`object`.value
    }
}