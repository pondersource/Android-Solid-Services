package com.pondersource.shared.vocab

/**
 * Web Access Control (WAC) vocabulary constants.
 * http://www.w3.org/ns/auth/acl#
 * Spec: https://solidproject.org/TR/wac
 */
object ACL {
    const val NAMESPACE = "http://www.w3.org/ns/auth/acl#"

    const val AUTHORIZATION = "${NAMESPACE}Authorization"

    const val READ = "${NAMESPACE}Read"
    const val WRITE = "${NAMESPACE}Write"
    const val APPEND = "${NAMESPACE}Append"
    /** Allows reading and modifying the ACL resource itself. */
    const val CONTROL = "${NAMESPACE}Control"

    //Authorization predicates
    /** Links an Authorization to a specific resource it protects. */
    const val ACCESS_TO = "${NAMESPACE}accessTo"

    /** Links an Authorization to a container; inherited by all members. */
    const val DEFAULT = "${NAMESPACE}default"

    /** Access mode(s) granted by this Authorization. */
    const val MODE = "${NAMESPACE}mode"

    /** Individual agent (by WebID) granted access. */
    const val AGENT = "${NAMESPACE}agent"

    /** Class of agents granted access (e.g. foaf:Agent, acl:AuthenticatedAgent). */
    const val AGENT_CLASS = "${NAMESPACE}agentClass"

    /** Group resource (vcard:Group) whose members are granted access. */
    const val AGENT_GROUP = "${NAMESPACE}agentGroup"

    /** HTTP Origin that is allowed to make requests. */
    const val ORIGIN = "${NAMESPACE}origin"

    //Agent class constants
    /** Any agent, authenticated or not. */
    const val AGENT_PUBLIC = "http://xmlns.com/foaf/0.1/Agent"

    /** Any authenticated agent. */
    const val AUTHENTICATED_AGENT = "${NAMESPACE}AuthenticatedAgent"

    //Legacy/storage
    /** Links a storage to its owner. */
    const val OWNER = "${NAMESPACE}owner"

    /** Trusted application with allowed origins and modes. */
    const val TRUSTED_APP = "${NAMESPACE}trustedApp"

    /** Access modes allowed for a trusted application. */
    const val TRUSTED_MODE = "${NAMESPACE}trustedMode"
}