package com.pondersource.shared.domain.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RDFResource
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.datamodule.contact.Group
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class GroupsIndexRDF : SolidRDFResource {

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    fun getGroups(addressBookUri: String): List<Group> =
        quads
            .filter { it.predicate == VCARD.INCLUDES_GROUP && it.subject == addressBookUri }
            .map { triple ->
                val groupName = quads.find {
                    it.subject == triple.`object` && it.predicate == VCARD.FN
                }!!.`object`
                Group(triple.`object`, groupName)
            }

    fun addGroup(addressBookUri: String, group: GroupRDF) {
        val groupUri = group.getIdentifier().toString()
        addQuad(groupUri, RDF.TYPE, VCARD.GROUP)
        addQuadLiteral(groupUri, VCARD.FN, group.getTitle(), null)
        addQuad(addressBookUri, VCARD.INCLUDES_GROUP, groupUri, maxNumber = Int.MAX_VALUE)
    }

    fun removeGroup(groupUri: URI): Boolean {
        val groupStr = groupUri.toString()
        val affected = quads.filter { it.subject == groupStr || it.`object` == groupStr }
        if (affected.isEmpty()) return false
        quads.removeAll(affected)
        return true
    }
}
