package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class GroupRDF: RDFSource {

    private val typeKey = rdf.createIRI(RDF.type.toString())
    private val fullNameKey = rdf.createIRI(VCARD.fn)
    private val hasMemberKey = rdf.createIRI(VCARD.hasMember)
    private val includesGroupKey = rdf.createIRI(VCARD.includesGroup)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)

    init {
        setType()
    }

    private fun setType() {
        addTriple(createTriple(typeKey, VCARD.Group))
    }

    fun getTitle() : String? {
        return dataset.defaultGraph.toList().find {
            it.subject.value == getIdentifier().toString() && it.predicate.equals(fullNameKey)
        }?.`object`?.value
    }

    fun getContacts(): List<Pair<String, String>> {
        val returnList = arrayListOf<Pair<String, String>>()
        val list = dataset.defaultGraph.toList()
        val members = list.filter { it.predicate.equals(hasMemberKey) }.map { triple ->
            val sameAs = list.find { it.subject.equals(triple.`object`) }
            if (sameAs == null ) {
                triple.`object`.value
            } else {
                sameAs.`object`.value
            }
        }
        members.forEach { member ->
            returnList.add(Pair(member, list.find { it.subject.value == member && it.predicate.equals(fullNameKey) }!!.`object`.value))
        }

        return returnList
    }

    fun includesInAddressBooks(): List<String> {
        return dataset.defaultGraph.toList().filter {
            it.predicate.equals(includesGroupKey)
        }.map { it.subject.value }
    }
}