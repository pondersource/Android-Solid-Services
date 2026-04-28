package com.pondersource.shared.domain.access

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.ACP
import com.pondersource.shared.vocab.RDF
import okhttp3.Headers
import java.net.URI

/**
 * Represents an ACP Access Control Resource (ACR).
 *
 * An ACR is linked to its subject resource via `Link: rel="acl"` and
 * carries `rdf:type acp:AccessControlResource`. It contains
 * `acp:AccessControl` nodes that reference `acp:Policy` documents.
 *
 * Spec: https://solidproject.org/TR/acp
 */
class SolidACR : SolidRDFResource {

    constructor(identifier: URI) : this(identifier, null, null)

    constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
        this(identifier, MediaType.JSON_LD, quads, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) : super(identifier, mediaType, quads, headers)

    /** Returns `true` if the quad store declares `rdf:type acp:AccessControlResource`. */
    fun isACR(): Boolean =
        quads.any { it.predicate == RDF.TYPE && it.`object` == ACP.ACCESS_CONTROL_RESOURCE }

    /**
     * Returns all `acp:AccessControl` IRIs referenced directly via
     * `acp:accessControl` on this ACR.
     */
    fun getAccessControls(): List<URI> =
        quads
            .filter { it.predicate == ACP.ACCESS_CONTROL }
            .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() }

    /**
     * Returns all `acp:AccessControl` IRIs that apply transitively to
     * member resources via `acp:memberAccessControl`.
     */
    fun getMemberAccessControls(): List<URI> =
        quads
            .filter { it.predicate == ACP.MEMBER_ACCESS_CONTROL }
            .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() }

    /**
     * Returns all `acp:Policy` IRIs referenced by any access control in
     * this ACR via `acp:apply`.
     */
    fun getPolicies(): List<URI> =
        quads
            .filter { it.predicate == ACP.APPLY }
            .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() }

    /**
     * Returns the access modes granted by a policy identified by [policyIri].
     */
    fun getAllowedModes(policyIri: String): Set<String> =
        quads
            .filter { it.subject == policyIri && it.predicate == ACP.ALLOW }
            .map { it.`object` }
            .toSet()

    /**
     * Returns the access modes denied by a policy identified by [policyIri].
     */
    fun getDeniedModes(policyIri: String): Set<String> =
        quads
            .filter { it.subject == policyIri && it.predicate == ACP.DENY }
            .map { it.`object` }
            .toSet()
}