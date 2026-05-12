package com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.Group
import com.erfangholami.androidsolidservices.shared.domain.resource.RdfQuad
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource
import com.erfangholami.androidsolidservices.shared.vocab.RDF
import com.erfangholami.androidsolidservices.shared.vocab.VCARD
import com.erfangholami.androidsolidservices.shared.vocab.XSD
import okhttp3.Headers
import java.net.URI

public class GroupsIndexRDF : SolidRDFResource {

    public constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    public fun getGroups(addressBookUri: String): List<Group> =
        quads
            .filter { it.predicate == VCARD.INCLUDES_GROUP && it.subject == addressBookUri }
            .mapNotNull { triple ->
                val groupName = quads.find {
                    it.subject == triple.`object` && it.predicate == VCARD.FN
                }?.`object` ?: return@mapNotNull null
                Group(triple.`object`, groupName)
            }

    public fun addGroup(addressBookUri: String, group: GroupRDF) {
        val groupUri = group.getIdentifier().toString()
        addQuad(groupUri, RDF.TYPE, VCARD.GROUP)
        addQuadLiteral(groupUri, VCARD.FN, group.getTitle(), XSD.STRING)
        addQuad(addressBookUri, VCARD.INCLUDES_GROUP, groupUri, maxNumber = Int.MAX_VALUE)
    }

    public fun removeGroup(groupUri: URI): Boolean {
        val groupStr = groupUri.toString()
        val affected = quads.filter { it.subject == groupStr || it.`object` == groupStr }
        if (affected.isEmpty()) return false
        quads.removeAll(affected)
        return true
    }
}
