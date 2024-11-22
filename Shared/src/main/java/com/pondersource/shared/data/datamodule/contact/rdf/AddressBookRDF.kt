package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.ACL
import com.pondersource.shared.vocab.DC
import com.pondersource.shared.vocab.ExtendedXsdConstants
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class AddressBookRDF: RDFSource {

    private val typeKey = rdf.createIRI(RDF.type.toString())
    private val typeValue = rdf.createIRI(VCARD.AddressBook)
    private val ownerKey = rdf.createIRI(ACL.owner)
    private val titleKey = rdf.createIRI(DC.title)
    private val nameEmailIndexKey = rdf.createIRI(VCARD.nameEmailIndex)
    private val groupIndexKey = rdf.createIRI(VCARD.groupIndex)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)

    init {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                typeKey,
                typeValue
            )
        )
    }

    fun getOwner(): String {
        return dataset.defaultGraph.toList().find {
            it.predicate.equals(titleKey)
        }!!.`object`.value
    }

    fun setOwner(owner: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                ownerKey,
                rdf.createIRI(owner)
            )
        )
    }

    fun getTitle(): String {
        return dataset.defaultGraph.toList().find {
            it.predicate.equals(titleKey)
        }!!.`object`.value
    }

    fun setTitle(title: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                titleKey,
                rdf.createTypedString(title, ExtendedXsdConstants.string)
            )
        )
    }

    fun getNameEmailIndex() : String {
        return dataset.defaultGraph.toList().find {
            it.predicate.equals(nameEmailIndexKey)
        }!!.`object`.value
    }

    fun setNameEmailIndex(peopleIndex: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                nameEmailIndexKey,
                rdf.createIRI(peopleIndex)
            )
        )
    }

    fun getGroupsIndex() : String {
        return dataset.defaultGraph.toList().find {
            it.predicate.equals(groupIndexKey)
        }!!.`object`.value
    }

    fun setGroupsIndex(groupsIndex: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                groupIndexKey,
                rdf.createIRI(groupsIndex)
            )
        )
    }
}