package com.pondersource.shared.domain.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.ACL
import com.pondersource.shared.vocab.DC
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.VCARD
import com.pondersource.shared.vocab.XSD
import okhttp3.Headers
import java.net.URI

class AddressBookRDF : SolidRDFResource {

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    init {
        addQuad(getIdentifier().toString(), RDF.TYPE, VCARD.ADDRESS_BOOK)
    }

    fun getOwner(): String =
        quads.find { it.predicate == ACL.OWNER }!!.`object`

    fun setOwner(owner: String) {
        addQuad(getIdentifier().toString(), ACL.OWNER, owner)
    }

    fun getTitle(): String =
        quads.find { it.predicate == DC.TITLE || it.predicate == DC.TITLE_LEGACY }!!.`object`

    fun setTitle(title: String) {
        addQuadLiteral(getIdentifier().toString(), DC.TITLE, title, XSD.STRING)
    }

    fun getNameEmailIndex(): String =
        quads.find { it.predicate == VCARD.NAME_EMAIL_INDEX }!!.`object`

    fun setNameEmailIndex(peopleIndex: String) {
        addQuad(getIdentifier().toString(), VCARD.NAME_EMAIL_INDEX, peopleIndex)
    }

    fun getGroupsIndex(): String =
        quads.find { it.predicate == VCARD.GROUP_INDEX }!!.`object`

    fun setGroupsIndex(groupsIndex: String) {
        addQuad(getIdentifier().toString(), VCARD.GROUP_INDEX, groupsIndex)
    }
}
