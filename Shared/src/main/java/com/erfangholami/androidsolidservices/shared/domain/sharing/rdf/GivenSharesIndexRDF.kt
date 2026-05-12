package com.erfangholami.androidsolidservices.shared.domain.sharing.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.resource.RdfQuad
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource
import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareMode
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareReceiver
import okhttp3.Headers
import java.net.URI

/**
 * RDF wrapper for `{podRoot}/.shares/given_shares.ttl`.
 *
 * Each share is represented as a single triple:
 *   `<receiver> <acl:Read|Append|Write> <resourceUri> .`
 *
 * - subject  = receiver (WebID, group URI, or `foaf:Agent` for public)
 * - predicate = WAC mode
 * - object   = resource URI
 *
 * The reflective constructor required by the Solid resource parser is provided.
 */
public class GivenSharesIndexRDF : SolidRDFResource {

    public constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null,
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    /** Returns every share triple in the document. */
    public fun getShares(): List<GivenShare> {
        return getAllQuads().mapNotNull { q ->
            val mode = ShareMode.fromAclPredicate(q.predicate) ?: return@mapNotNull null
            val receiver = ShareReceiver.from(q.subject)
            GivenShare(
                receiver = receiver,
                mode = mode,
                resourceUri = q.`object`,
            )
        }
    }
}
