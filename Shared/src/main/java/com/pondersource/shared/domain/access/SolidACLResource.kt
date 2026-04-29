package com.pondersource.shared.domain.access

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.ACL
import com.pondersource.shared.vocab.RDF
import okhttp3.Headers
import java.net.URI

/**
 * Represents a Web Access Control (WAC) ACL resource.
 *
 * An ACL resource is an RDF document containing one or more
 * `acl:Authorization` rules that govern access to the associated
 * subject resource or a container's member resources.
 *
 * Advertised via `Link: <acl-uri>; rel="acl"` on the subject resource.
 *
 * Spec: https://solidproject.org/TR/wac
 */
class SolidACLResource : SolidRDFResource {

    constructor(identifier: URI) : this(identifier, null, null)

    constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
            this(identifier, MediaType.JSON_LD, quads, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) : super(identifier, mediaType, quads, headers)

    /**
     * Returns all `acl:Authorization` instances in this ACL document.
     */
    fun getAuthorizations(): List<AclAuthorization> {
        val authSubjects = quads
            .filter { it.predicate == RDF.TYPE && it.`object` == ACL.AUTHORIZATION }
            .map { it.subject }
            .distinct()

        return authSubjects.map { subjectIri ->
            val forSubject = quads.filter { it.subject == subjectIri }

            AclAuthorization(
                subject = subjectIri,

                accessTo = forSubject
                    .filter { it.predicate == ACL.ACCESS_TO }
                    .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() },

                default = forSubject
                    .filter { it.predicate == ACL.DEFAULT }
                    .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() },

                modes = forSubject
                    .filter { it.predicate == ACL.MODE }
                    .map { it.`object` }
                    .toSet(),

                agents = forSubject
                    .filter { it.predicate == ACL.AGENT }
                    .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() },

                agentClasses = forSubject
                    .filter { it.predicate == ACL.AGENT_CLASS }
                    .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() },

                agentGroups = forSubject
                    .filter { it.predicate == ACL.AGENT_GROUP }
                    .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() },

                origins = forSubject
                    .filter { it.predicate == ACL.ORIGIN }
                    .mapNotNull { runCatching { URI.create(it.`object`) }.getOrNull() },
            )
        }
    }

    /**
     * Adds a new authorization to this ACL document.
     */
    fun addAuthorization(authorization: AclAuthorization) {
        val subject = authorization.subject
        addQuad(subject, RDF.TYPE, ACL.AUTHORIZATION, maxNumber = Int.MAX_VALUE)
        authorization.accessTo.forEach {
            addQuad(
                subject,
                ACL.ACCESS_TO,
                it.toString(),
                maxNumber = Int.MAX_VALUE
            )
        }
        authorization.default.forEach {
            addQuad(
                subject,
                ACL.DEFAULT,
                it.toString(),
                maxNumber = Int.MAX_VALUE
            )
        }
        authorization.modes.forEach { addQuad(subject, ACL.MODE, it, maxNumber = Int.MAX_VALUE) }
        authorization.agents.forEach {
            addQuad(
                subject,
                ACL.AGENT,
                it.toString(),
                maxNumber = Int.MAX_VALUE
            )
        }
        authorization.agentClasses.forEach {
            addQuad(
                subject,
                ACL.AGENT_CLASS,
                it.toString(),
                maxNumber = Int.MAX_VALUE
            )
        }
        authorization.agentGroups.forEach {
            addQuad(
                subject,
                ACL.AGENT_GROUP,
                it.toString(),
                maxNumber = Int.MAX_VALUE
            )
        }
        authorization.origins.forEach {
            addQuad(
                subject,
                ACL.ORIGIN,
                it.toString(),
                maxNumber = Int.MAX_VALUE
            )
        }
    }
}