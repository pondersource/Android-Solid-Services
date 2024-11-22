package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.data.datamodule.contact.Contact
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class NameEmailIndexRDF: RDFSource {

    private val rdfKey = rdf.createIRI(RDF.type.toString())
    private val individualKey = rdf.createIRI(VCARD.Individual)
    private val fullNameKey = rdf.createIRI(VCARD.fn)
    private val inAddressBookKey = rdf.createIRI(VCARD.inAddressBook)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)

    fun getContacts(addressBookUri: String): List<Contact> {
        val returnList = arrayListOf<Contact>()
        val list = dataset.defaultGraph.toList()
        list.filter { it.predicate.equals(inAddressBookKey) && it.subject.value == addressBookUri } .forEach { triple ->
            val contactUri = URI.create(triple.`object`.value)
            val contactName = list.find { it.subject.equals(triple.`object`) && it.predicate.equals(fullNameKey) }!!.`object`.value
            returnList.add(Contact(contactUri, contactName))
        }
        return returnList
    }

    fun addContact(addressBookUri: String, contact: ContactRDF) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(addressBookUri),
                inAddressBookKey,
                rdf.createIRI(contact.getIdentifier().toString())
            ),
            Int.MAX_VALUE
        )
        addTriple(
            rdf.createTriple(
                rdf.createIRI(contact.getIdentifier().toString()),
                rdfKey,
                individualKey
            ),
        )
        addTriple(
            rdf.createTriple(
                rdf.createIRI(contact.getIdentifier().toString()),
                fullNameKey,
                rdf.createTypedString(contact.getFullName(), null)
            ),
        )
    }

    fun removeContact(addressBookUri: String, contactUri: String): Boolean {
        val list = dataset.defaultGraph.toList()
        val contactTriples = list.filter {
            (it.subject.value == addressBookUri && it.predicate.equals(inAddressBookKey) && it.`object`.value == contactUri) ||
            (it.subject.value == contactUri && it.predicate.equals(rdfKey) && it.`object`.equals(individualKey)) ||
            (it.subject.value == contactUri && it.predicate.equals(fullNameKey))
        }
        if (contactTriples.isNotEmpty()) {
            list.removeAll(contactTriples)
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