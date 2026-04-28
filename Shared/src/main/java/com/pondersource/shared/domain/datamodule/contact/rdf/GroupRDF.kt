package com.pondersource.shared.domain.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RDFResource
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.datamodule.contact.Contact
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.OWL
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class GroupRDF : SolidRDFResource {

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    init {
        addQuad(getIdentifier().toString(), RDF.TYPE, VCARD.GROUP)
    }

    fun getTitle(): String =
        quads.find {
            it.subject == getIdentifier().toString() && it.predicate == VCARD.FN
        }!!.`object`

    fun setTitle(title: String) {
        addQuadLiteral(getIdentifier().toString(), VCARD.FN, title, null)
    }

    fun setIncludesInAddressBook(addressBookUri: String) {
        addQuad(addressBookUri, VCARD.INCLUDES_GROUP, getIdentifier().toString())
    }

    fun getContacts(): List<Contact> {
        val members = quads
            .filter { it.predicate == VCARD.HAS_MEMBER }
            .map { triple ->
                val sameAs = quads.find {
                    it.predicate == OWL.SAME_AS && it.subject == triple.`object`
                }
                sameAs?.`object` ?: triple.`object`
            }

        return members.map { memberUri ->
            val name = quads.find {
                it.subject == memberUri && it.predicate == VCARD.FN
            }!!.`object`
            Contact(memberUri, name)
        }
    }

    fun addMember(contact: ContactRDF) {
        addQuad(getIdentifier().toString(), VCARD.HAS_MEMBER, contact.getIdentifier().toString(), maxNumber = Int.MAX_VALUE)
        addQuadLiteral(contact.getIdentifier().toString(), VCARD.FN, contact.getFullName(), null)
    }

    fun removeMember(contactURI: URI): Boolean {
        val contactStr = contactURI.toString()
        val member = quads.find {
            it.subject == getIdentifier().toString() &&
            it.predicate == VCARD.HAS_MEMBER &&
            it.`object` == contactStr
        } ?: return false

        val memberNameQuads = quads.filter {
            it.subject == contactStr && it.predicate == VCARD.FN
        }
        quads.remove(member)
        quads.removeAll(memberNameQuads)
        return true
    }
}
