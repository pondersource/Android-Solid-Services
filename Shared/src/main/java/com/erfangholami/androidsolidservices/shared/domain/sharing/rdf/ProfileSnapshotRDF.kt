package com.erfangholami.androidsolidservices.shared.domain.sharing.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.resource.RdfQuad
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource
import com.erfangholami.androidsolidservices.shared.domain.sharing.ProfileField
import com.erfangholami.androidsolidservices.shared.vocab.DC
import com.erfangholami.androidsolidservices.shared.vocab.FOAF
import com.erfangholami.androidsolidservices.shared.vocab.RDF
import okhttp3.Headers
import java.net.URI

/**
 * RDF wrapper for a profile snapshot document — `{podRoot}/solidshare/profiles/{uuid}.ttl`.
 *
 * The snapshot's `#me` subject carries the user-selected fields plus a
 * `dcterms:creator` triple pointing at the owner's WebID so a receiver can
 * track who shared it.
 */
public class ProfileSnapshotRDF : SolidRDFResource {

    public constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null,
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    public fun meSubject(): String = "${getIdentifier()}#me"

    public fun setOwner(webId: String) {
        addQuad(meSubject(), DC.CREATOR, webId)
    }

    public fun getOwner(): String? = findProperty(DC.CREATOR)

    public fun markAsAgent() {
        addQuad(meSubject(), RDF.TYPE, FOAF.AGENT)
    }

    /** Adds a literal value for the predicate of [field]. */
    public fun setLiteral(field: ProfileField, value: String) {
        addQuadLiteral(meSubject(), field.predicate, value)
    }

    /** Adds an IRI value for the predicate of [field]. */
    public fun setIri(field: ProfileField, value: String) {
        addQuad(meSubject(), field.predicate, value)
    }

    /** Returns the literal value for [field], or null if absent. */
    public fun getLiteral(field: ProfileField): String? =
        findProperty(field.predicate)

    /** Returns the IRI value for [field], or null if absent. */
    public fun getIri(field: ProfileField): String? = findProperty(field.predicate)
}
