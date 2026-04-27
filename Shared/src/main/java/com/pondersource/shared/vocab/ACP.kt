package com.pondersource.shared.vocab

/**
 * Access Control Policy (ACP) vocabulary constants.
 * http://www.w3.org/ns/solid/acp#
 * Spec: https://solidproject.org/TR/acp
 */
object ACP {
    const val NAMESPACE = "http://www.w3.org/ns/solid/acp#"

    //Types
    /** Connects a resource to its access controls. Linked via Link: rel="acl". */
    const val ACCESS_CONTROL_RESOURCE = "${NAMESPACE}AccessControlResource"

    /** Connects an AccessControlResource to Policies. */
    const val ACCESS_CONTROL_TYPE = "${NAMESPACE}AccessControl"

    /** Defines allowed/denied access modes and matcher conditions. */
    const val POLICY = "${NAMESPACE}Policy"

    /** Describes matching context attributes for a policy. */
    const val MATCHER = "${NAMESPACE}Matcher"

    /** Extensible base class for access modes. */
    const val ACCESS_MODE = "${NAMESPACE}AccessMode"

    /** Describes a resource access instance (target, agent, client, issuer). */
    const val CONTEXT_TYPE = "${NAMESPACE}Context"

    /** Defines access modes granted in a given context. */
    const val ACCESS_GRANT = "${NAMESPACE}AccessGrant"

    //ACR predicates
    /** Connects an ACR to the resource it controls. */
    const val RESOURCE = "${NAMESPACE}resource"

    /** Connects an ACR to access controls that apply directly to the resource. */
    const val ACCESS_CONTROL = "${NAMESPACE}accessControl"

    /** Connects an ACR to access controls that apply transitively to member resources. */
    const val MEMBER_ACCESS_CONTROL = "${NAMESPACE}memberAccessControl"

    //AccessControl predicates
    /** References policies applied by this access control. */
    const val APPLY = "${NAMESPACE}apply"

    //Policy predicates
    /** Access modes granted when the policy is satisfied. */
    const val ALLOW = "${NAMESPACE}allow"

    /** Access modes denied when the policy is satisfied. */
    const val DENY = "${NAMESPACE}deny"

    /** All referenced matchers must be satisfied. */
    const val ALL_OF = "${NAMESPACE}allOf"

    /** At least one referenced matcher must be satisfied. */
    const val ANY_OF = "${NAMESPACE}anyOf"

    /** None of the referenced matchers must be satisfied. */
    const val NONE_OF = "${NAMESPACE}noneOf"

    //Matcher/ Context attributes
    const val AGENT = "${NAMESPACE}agent"
    const val CLIENT = "${NAMESPACE}client"
    const val ISSUER = "${NAMESPACE}issuer"
    const val VC = "${NAMESPACE}vc"
    const val TARGET = "${NAMESPACE}target"
    const val MODE = "${NAMESPACE}mode"
    const val CREATOR = "${NAMESPACE}creator"
    const val OWNER = "${NAMESPACE}owner"

    /** Base property for custom context attributes. */
    const val ATTRIBUTE = "${NAMESPACE}attribute"

    //AccessGrant predicates
    /** Access modes granted in this context. */
    const val GRANT = "${NAMESPACE}grant"

    /** Context associated with this access grant. */
    const val CONTEXT = "${NAMESPACE}context"

    //Special named individuals
    /** Matches all agents (authenticated and public). */
    const val PUBLIC_AGENT = "${NAMESPACE}PublicAgent"

    /** Matches only authenticated agents. */
    const val AUTHENTICATED_AGENT = "${NAMESPACE}AuthenticatedAgent"

    /** Matches when the requesting agent is the resource creator. */
    const val CREATOR_AGENT = "${NAMESPACE}CreatorAgent"

    /** Matches when the requesting agent is the storage owner. */
    const val OWNER_AGENT = "${NAMESPACE}OwnerAgent"

    /** Matches all client applications. */
    const val PUBLIC_CLIENT = "${NAMESPACE}PublicClient"

    /** Matches all issuers. */
    const val PUBLIC_ISSUER = "${NAMESPACE}PublicIssuer"

    /** Matcher that is always satisfied. */
    const val ALWAYS_SATISFIED_RESTRICTION = "${NAMESPACE}AlwaysSatisfiedRestriction"
}
