package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class GroupsIndexRDF: RDFSource {

    private val typeKey = rdf.createIRI(RDF.type.toString())
    private val fullNameKey = rdf.createIRI(VCARD.fn)
    private val includesGroupKey = rdf.createIRI(VCARD.includesGroup)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)

    //Returns a list of groups with their identifiers and names
    fun getGroups(): List<Pair<String, String>> {
        val returnList = arrayListOf<Pair<String, String>>()
        val list = dataset.defaultGraph.toList()
        list.forEach {triple ->
            if(triple.predicate.equals(typeKey)){
                val name = list.find { it.subject.equals(triple.subject) && it.predicate.equals(fullNameKey) }!!.`object`.value
                returnList.add(Pair(triple.subject.value, name))
            }
        }
        return returnList
    }

    fun getAddressBookGroupPair() : List<Pair<String, List<String>>> {
        val returnList = arrayListOf<Pair<String, List<String>>>()
        val list = dataset.defaultGraph.toList().filter { it.predicate.equals(includesGroupKey) }
        val addressBooks = list.distinctBy { it.subject }
        addressBooks.forEach { triple ->
            returnList.add(Pair(triple.subject.value, list.filter { it.subject.equals(triple.subject) }.map { it.`object`.value }))
        }
        return returnList
    }
}