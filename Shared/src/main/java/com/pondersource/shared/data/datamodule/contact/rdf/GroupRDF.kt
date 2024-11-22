package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.data.datamodule.contact.Contact
import com.pondersource.shared.vocab.OWL
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class GroupRDF: RDFSource {

    private val typeKey = rdf.createIRI(RDF.type.toString())
    private val typeValue = rdf.createIRI(VCARD.Group)
    private val fullNameKey = rdf.createIRI(VCARD.fn)
    private val hasMemberKey = rdf.createIRI(VCARD.hasMember)
    private val includesGroupKey = rdf.createIRI(VCARD.includesGroup)
    private val sameAsKey = rdf.createIRI(OWL.sameAs)

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
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                typeKey,
                typeValue
            )
        )
    }

    fun getTitle() : String {
        return dataset.defaultGraph.toList().find {
            it.subject.value == getIdentifier().toString() && it.predicate.equals(fullNameKey)
        }!!.`object`!!.value
    }

    fun setTitle(title: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                fullNameKey,
                rdf.createTypedString(title, null)
            )
        )
    }

    fun setIncludesInAddressBook(addressBookUri: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(addressBookUri),
                includesGroupKey,
                rdf.createIRI(getIdentifier().toString())
            )
        )
    }

    fun getContacts(): List<Contact> {
        val returnList = arrayListOf<Contact>()
        val list = dataset.defaultGraph.toList()
        val members = list.filter { it.predicate.equals(hasMemberKey) }.map { triple ->
            val sameAs = list.find { it.predicate.equals(sameAsKey) && it.subject.equals(triple.`object`) }
            return@map if (sameAs == null ) {
                triple.`object`.value
            } else {
                sameAs.`object`.value
            }
        }
        members.forEach { member ->
            returnList.add(
                Contact(
                    URI.create(member),
                    list.find { it.subject.value == member && it.predicate.equals(fullNameKey) }!!.`object`.value
                )
            )
        }

        return returnList
    }

    fun addMember(contact: ContactRDF) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                hasMemberKey,
                rdf.createIRI(contact.getIdentifier().toString())
            ),
            Int.MAX_VALUE
        )

        addTriple(
            rdf.createTriple(
                rdf.createIRI(contact.getIdentifier().toString()),
                fullNameKey,
                rdf.createTypedString(contact.getFullName(), null)
            )
        )
    }

    /**
     * @param contactURI of which we want to remove from the group
     * @return true if dataset has changed
     */
    fun removeMember(contactURI: URI): Boolean {
        val list = dataset.defaultGraph.toList()
        val member = list.find { it.subject.value == getIdentifier().toString() && it.predicate.equals(hasMemberKey) && it.`object`.value == contactURI.toString() }
        if (member != null) {
            val memberName = list.filter { it.subject.equals(member.`object`) && it.predicate.equals(fullNameKey) }
            val newList = list.filter { it != member && it != memberName }
            dataset = rdf.createDataset().apply {
                newList.forEach {
                    add(it)
                }
            }
            return true
        } else {
            return false
        }
    }
}