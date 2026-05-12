package com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.resource.RdfQuad
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource
import com.erfangholami.androidsolidservices.shared.vocab.ACL
import com.erfangholami.androidsolidservices.shared.vocab.DC
import com.erfangholami.androidsolidservices.shared.vocab.RDF
import com.erfangholami.androidsolidservices.shared.vocab.VCARD
import com.erfangholami.androidsolidservices.shared.vocab.XSD
import okhttp3.Headers
import java.net.URI

public class AddressBookRDF : SolidRDFResource {

    public constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    init {
        addQuad(getIdentifier().toString(), RDF.TYPE, VCARD.ADDRESS_BOOK)
    }

    public fun getOwner(): String =
        quads.find { it.predicate == ACL.OWNER }!!.`object`

    public fun setOwner(owner: String) {
        addQuad(getIdentifier().toString(), ACL.OWNER, owner)
    }

    public fun getTitle(): String =
        quads.find { it.predicate == DC.TITLE || it.predicate == DC.TITLE_LEGACY }!!.`object`

    public fun setTitle(title: String) {
        addQuadLiteral(getIdentifier().toString(), DC.TITLE, title, XSD.STRING)
    }

    public fun getNameEmailIndex(): String =
        quads.find { it.predicate == VCARD.NAME_EMAIL_INDEX }!!.`object`

    public fun setNameEmailIndex(peopleIndex: String) {
        addQuad(getIdentifier().toString(), VCARD.NAME_EMAIL_INDEX, peopleIndex)
    }

    public fun getGroupsIndex(): String =
        quads.find { it.predicate == VCARD.GROUP_INDEX }!!.`object`

    public fun setGroupsIndex(groupsIndex: String) {
        addQuad(getIdentifier().toString(), VCARD.GROUP_INDEX, groupsIndex)
    }
}
