package com.pondersource.shared.domain.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RDFResource
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.datamodule.contact.Email
import com.pondersource.shared.domain.datamodule.contact.Name
import com.pondersource.shared.domain.datamodule.contact.PhoneNumber
import com.pondersource.shared.domain.datamodule.contact.URLType
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.VCARD
import com.pondersource.shared.vocab.XSD
import okhttp3.Headers
import java.net.URI

class ContactRDF : SolidRDFResource {

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    init {
        addQuad(getIdentifier().toString(), RDF.TYPE, VCARD.INDIVIDUAL)
    }

    fun getFullName(): String =
        quads.find {
            it.subject == getIdentifier().toString() && it.predicate == VCARD.FN
        }!!.`object`

    fun setFullName(name: String) {
        addQuadLiteral(getIdentifier().toString(), VCARD.FN, name, XSD.STRING)
    }

    fun getPhotoUrl(): String? =
        quads.find {
            it.subject == getIdentifier().toString() && it.predicate == VCARD.HAS_PHOTO
        }?.`object`

    fun getUrls(): List<Pair<URLType, String>> {
        val selfUri = getIdentifier().toString()
        return quads
            .filter { it.subject == selfUri && it.predicate == VCARD.URL }
            .mapNotNull { urlTriple ->
                val urlNode = urlTriple.`object`
                val type: URLType = when (
                    quads.find { it.subject == urlNode && it.predicate == RDF.TYPE }?.`object`
                ) {
                    VCARD.HOME      -> URLType.Home
                    VCARD.WORK      -> URLType.Work
                    VCARD.HOMEPAGE  -> URLType.Homepage
                    VCARD.WEB_ID     -> URLType.WebId
                    VCARD.PUBLIC_ID  -> URLType.PublicId
                    else            -> URLType.Home
                }
                val url = quads.find { it.subject == urlNode && it.predicate == VCARD.VALUE }
                    ?.`object` ?: return@mapNotNull null
                Pair(type, url)
            }
    }

    fun getName(): Name? {
        val hasNameQuad = quads.find {
            it.subject == getIdentifier().toString() && it.predicate == VCARD.HAS_NAME
        } ?: return null
        val nameNode = hasNameQuad.`object`
        return Name(
            quads.find { it.subject == nameNode && it.predicate == VCARD.FAMILY_NAME }?.`object`,
            quads.find { it.subject == nameNode && it.predicate == VCARD.GIVEN_NAME }?.`object`,
            quads.find { it.subject == nameNode && it.predicate == VCARD.ADDITIONAL_NAME }?.`object`,
            quads.find { it.subject == nameNode && it.predicate == VCARD.HONORIFIC_PREFIX }?.`object`,
            quads.find { it.subject == nameNode && it.predicate == VCARD.HONORIFIC_SUFFIX }?.`object`,
        )
    }

    fun getPhoneNumbers(): List<PhoneNumber> =
        quads
            .filter { it.subject == getIdentifier().toString() && it.predicate == VCARD.HAS_TELEPHONE }
            .mapNotNull { triple ->
                quads.find { it.subject == triple.`object` && it.predicate == VCARD.VALUE }
                    ?.let { PhoneNumber(it.`object`) }
            }

    fun addPhoneNumber(newPhoneNumber: String?): Boolean {
        if (newPhoneNumber.isNullOrEmpty()) return false
        val telValue = "tel:$newPhoneNumber"
        if (quads.any { it.predicate == VCARD.VALUE && it.`object` == telValue }) return false
        val blankNode = "_:$newPhoneNumber"
        addQuad(getIdentifier().toString(), VCARD.HAS_TELEPHONE, blankNode, maxNumber = Int.MAX_VALUE)
        addQuadLiteral(blankNode, VCARD.VALUE, telValue, null)
        return true
    }

    fun getEmails(): List<Email> =
        quads
            .filter { it.subject == getIdentifier().toString() && it.predicate == VCARD.HAS_EMAIL }
            .mapNotNull { triple ->
                quads.find { it.subject == triple.`object` && it.predicate == VCARD.VALUE }
                    ?.let { Email(it.`object`) }
            }

    fun addEmailAddress(newEmailAddress: String?): Boolean {
        if (newEmailAddress.isNullOrEmpty()) return false
        val mailtoValue = "mailto:$newEmailAddress"
        if (quads.any { it.predicate == VCARD.VALUE && it.`object` == mailtoValue }) return false
        val blankNode = "_:$newEmailAddress"
        addQuad(getIdentifier().toString(), VCARD.HAS_EMAIL, blankNode, maxNumber = Int.MAX_VALUE)
        addQuadLiteral(blankNode, VCARD.VALUE, mailtoValue, null)
        return true
    }

    fun removePhoneNumber(phoneNumber: String): Boolean {
        val telValue = "tel:$phoneNumber"
        val valueQuad = quads.find { it.predicate == VCARD.VALUE && it.`object` == telValue }
            ?: return false
        val hasTelQuad = quads.find {
            it.`object` == valueQuad.subject && it.predicate == VCARD.HAS_TELEPHONE
        }
        quads.remove(valueQuad)
        if (hasTelQuad != null) quads.remove(hasTelQuad)
        return true
    }

    fun removeEmailAddress(emailAddress: String): Boolean {
        val mailtoValue = "mailto:$emailAddress"
        val valueQuad = quads.find { it.predicate == VCARD.VALUE && it.`object` == mailtoValue }
            ?: return false
        val hasEmailQuad = quads.find {
            it.`object` == valueQuad.subject && it.predicate == VCARD.HAS_EMAIL
        }
        quads.remove(valueQuad)
        if (hasEmailQuad != null) quads.remove(hasEmailQuad)
        return true
    }
}
