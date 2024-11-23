package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.data.datamodule.contact.Group
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class GroupsIndexRDF: RDFSource {

    private val typeKey = rdf.createIRI(RDF.type.toString())
    private val groupValue = rdf.createIRI(VCARD.Group)
    private val fullNameKey = rdf.createIRI(VCARD.fn)
    private val includesGroupKey = rdf.createIRI(VCARD.includesGroup)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)

    //Returns a list of groups with their identifiers and names
    fun getGroups(addressBookUri: String): List<Group> {
        val returnList = arrayListOf<Group>()
        val list = dataset.defaultGraph.toList()
        list.filter { it.predicate.equals(includesGroupKey) && it.subject.value == addressBookUri } .forEach { triple ->
            val groupName = list.find { it.subject.equals(triple.`object`) && it.predicate.equals(fullNameKey) }!!.`object`.value
            returnList.add(Group(triple.`object`.value, groupName))
        }
        return returnList
    }

    fun addGroup(addressBookUri: String, group : GroupRDF) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(group.getIdentifier().toString()),
                typeKey,
                groupValue
            )
        )

        addTriple(
            rdf.createTriple(
                rdf.createIRI(group.getIdentifier().toString()),
                fullNameKey,
                rdf.createTypedString(group.getTitle(), null)
            )
        )

        addTriple(
            rdf.createTriple(
                rdf.createIRI(addressBookUri),
                includesGroupKey,
                rdf.createIRI(group.getIdentifier().toString())
            ),
            Int.MAX_VALUE
        )
    }

    fun removeGroup(groupUri: URI): Boolean {
        val list = dataset.defaultGraph.toList()
        val groupTriples = list.filter { it.subject.value == groupUri.toString() || it.`object`.value == groupUri.toString() }
        if(groupTriples.isNotEmpty()) {
            list.removeAll(groupTriples)
            dataset = rdf.createDataset().apply {
                list.forEach {
                    add(it)
                }
            }
            return true
        } else {
            return false
        }

    }
}