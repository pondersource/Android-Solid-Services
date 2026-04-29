package com.pondersource.shared.domain.access

import com.pondersource.shared.vocab.ACL
import com.pondersource.shared.vocab.Solid
import java.net.URI

/**
 * A single WAC authorization rule (`acl:Authorization`).
 *
 * Each authorization grants a set of [modes] to a set of [agents] /
 * [agentClasses] / [agentGroups] on the resources identified by
 * [accessTo] (direct) or inherited by members of containers in [default].
 *
 * Spec: https://solidproject.org/TR/wac
 */
data class AclAuthorization(
    /** Identifier of this authorization node (blank node or IRI). */
    val subject: String,

    /** IRIs of specific resources this authorization applies to directly. */
    val accessTo: List<URI> = emptyList(),

    /**
     * IRIs of containers whose members inherit this authorization
     * (acl:default).
     */
    val default: List<URI> = emptyList(),

    /**
     * Access modes granted (e.g. `acl:Read`, `acl:Write`, `acl:Append`,
     * `acl:Control`).
     */
    val modes: Set<String> = emptySet(),

    /** Individual agents granted access, identified by WebID. */
    val agents: List<URI> = emptyList(),

    /**
     * Agent classes granted access.
     * Common values: `foaf:Agent` (public), `acl:AuthenticatedAgent`.
     */
    val agentClasses: List<URI> = emptyList(),

    /**
     * Group resources (`vcard:Group`) whose members are granted access.
     */
    val agentGroups: List<URI> = emptyList(),

    /**
     * HTTP Origins that are permitted for this authorization.
     * Empty means all origins are permitted.
     */
    val origins: List<URI> = emptyList(),
) {
    fun allowsRead() = modes.contains(ACL.READ)
    fun allowsWrite() = modes.contains(ACL.WRITE)
    fun allowsAppend() = modes.contains(ACL.APPEND) || allowsWrite()
    fun allowsControl() = modes.contains(ACL.CONTROL)

    /** Returns `true` if this authorization applies to the public (`foaf:Agent` agentClass). */
    fun isPublic() = agentClasses.any { it.toString() == Solid.PUBLIC_AGENT }
}
