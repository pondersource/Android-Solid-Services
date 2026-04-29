package com.pondersource.shared.domain.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.datamodule.contact.Contact
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class NameEmailIndexRDF : SolidRDFResource {

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    fun getContacts(addressBookUri: String): List<Contact> =
        quads
            .filter { it.predicate == VCARD.IN_ADDRESS_BOOK && it.subject == addressBookUri }
            .map { triple ->
                val contactName = quads.find {
                    it.subject == triple.`object` && it.predicate == VCARD.FN
                }!!.`object`
                Contact(triple.`object`, contactName)
            }

    fun addContact(addressBookUri: String, contact: ContactRDF) {
        val contactUri = contact.getIdentifier().toString()
        addQuad(addressBookUri, VCARD.IN_ADDRESS_BOOK, contactUri, maxNumber = Int.MAX_VALUE)
        addQuad(contactUri, RDF.TYPE, VCARD.INDIVIDUAL)
        addQuadLiteral(contactUri, VCARD.FN, contact.getFullName(), null)
    }

    fun removeContact(contactUri: String): Boolean {
        val affected = quads.filter {
            (it.predicate == VCARD.IN_ADDRESS_BOOK && it.`object` == contactUri) ||
                    it.subject == contactUri
        }
        if (affected.isEmpty()) return false
        quads.removeAll { it.subject == contactUri || it.`object` == contactUri }
        return true
    }
}
