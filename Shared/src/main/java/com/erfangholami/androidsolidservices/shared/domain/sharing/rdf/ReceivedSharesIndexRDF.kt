package com.erfangholami.androidsolidservices.shared.domain.sharing.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.resource.RdfQuad
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource
import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareMode
import okhttp3.Headers
import java.net.URI

/**
 * RDF wrapper for `{podRoot}/.shares/received_shares.ttl`.
 *
 * Each received share is represented as a triple:
 *   `<ownerWebId> <acl:Read|Append|Write> <resourceUri> .`
 */
public class ReceivedSharesIndexRDF : SolidRDFResource {

    public constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        quads: List<RdfQuad>? = null,
        headers: Headers? = null,
    ) : super(identifier, mediaType ?: MediaType.JSON_LD, quads, headers)

    public fun getShares(): List<ReceivedShare> {
        return getAllQuads().mapNotNull { q ->
            val mode = ShareMode.fromAclPredicate(q.predicate) ?: return@mapNotNull null
            ReceivedShare(
                ownerWebId = q.subject,
                mode = mode,
                resourceUri = q.`object`,
            )
        }
    }
}
