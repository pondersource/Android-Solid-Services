package com.pondersource.shared.vocab

/**
 * Solid Application Interoperability (SAI) vocabulary constants.
 * http://www.w3.org/ns/solid/interop#
 * Spec: https://solidproject.org/TR/sai
 *
 * SAI defines how applications request and receive delegated access to
 * data across Solid pods via registries, access needs, and access grants.
 */
object SAI {
    const val NAMESPACE = "http://www.w3.org/ns/solid/interop#"

    //Agent types
    /** An individual, group, or organization identifiable by a WebID. */
    const val SOCIAL_AGENT = "${NAMESPACE}SocialAgent"

    /** A software-based agent (app) used to access and manage pod data. */
    const val APPLICATION = "${NAMESPACE}Application"

    /** The authorization agent application managing data access on behalf of an agent. */
    const val AUTHORIZATION_AGENT = "${NAMESPACE}AuthorizationAgent"

    //Registry types
    /** Links to all registries owned by a social agent. */
    const val REGISTRY_SET = "${NAMESPACE}RegistrySet"

    /** A collection of agent registrations (known apps and social agents). */
    const val AGENT_REGISTRY = "${NAMESPACE}AgentRegistry"

    /** A collection of data registrations for a specific shape tree. */
    const val DATA_REGISTRY = "${NAMESPACE}DataRegistry"

    /** Stores data instances for a specific shape tree within a data registry. */
    const val DATA_REGISTRATION = "${NAMESPACE}DataRegistration"

    /** Records metadata about an application used by a social agent. */
    const val APPLICATION_REGISTRATION = "${NAMESPACE}ApplicationRegistration"

    /** Records another social agent that has been interacted with. */
    const val SOCIAL_AGENT_REGISTRATION = "${NAMESPACE}SocialAgentRegistration"

    /** A secure way to initiate a data-sharing relationship. */
    const val SOCIAL_AGENT_INVITATION = "${NAMESPACE}SocialAgentInvitation"

    //Access need types
    /** A group of related access needs communicated together. */
    const val ACCESS_NEED_GROUP = "${NAMESPACE}AccessNeedGroup"

    /** A requirement for access to one specific data type / shape tree. */
    const val ACCESS_NEED = "${NAMESPACE}AccessNeed"

    /** Sent from one agent to another to communicate access need groups. */
    const val ACCESS_REQUEST = "${NAMESPACE}AccessRequest"

    //Authorization / grant types
    /** Records the decision to grant access to data for an agent. */
    const val ACCESS_AUTHORIZATION = "${NAMESPACE}AccessAuthorization"

    /** Authorizes access to a specific data type within an access authorization. */
    const val DATA_AUTHORIZATION = "${NAMESPACE}DataAuthorization"

    /** Describes the access that has been granted to an agent. */
    const val ACCESS_GRANT = "${NAMESPACE}AccessGrant"

    /** Detailed access description for a specific data type within an access grant. */
    const val DATA_GRANT = "${NAMESPACE}DataGrant"

    /** A data grant that re-shares access from a grantee to another agent. */
    const val DELEGATED_DATA_GRANT = "${NAMESPACE}DelegatedDataGrant"

    //Registry predicates
    /** Links a social agent to their registry set. */
    const val HAS_REGISTRY_SET = "${NAMESPACE}hasRegistrySet"

    /** Links a social agent to their authorization agent application. */
    const val HAS_AUTHORIZATION_AGENT = "${NAMESPACE}hasAuthorizationAgent"

    /** Links a registry set to an agent registry. */
    const val HAS_AGENT_REGISTRY = "${NAMESPACE}hasAgentRegistry"

    /** Links a registry set to a data registry. */
    const val HAS_DATA_REGISTRY = "${NAMESPACE}hasDataRegistry"

    /** Links an agent registry to a social agent registration. */
    const val HAS_SOCIAL_AGENT_REGISTRATION = "${NAMESPACE}hasSocialAgentRegistration"

    /** Links an agent registry to an application registration. */
    const val HAS_APPLICATION_REGISTRATION = "${NAMESPACE}hasApplicationRegistration"

    /** Links a data registry to a data registration. */
    const val HAS_DATA_REGISTRATION = "${NAMESPACE}hasDataRegistration"

    /** Links a data registration to a specific data instance. */
    const val HAS_DATA_INSTANCE = "${NAMESPACE}hasDataInstance"

    //Registration predicates
    /** The social agent that created this registration. */
    const val REGISTERED_BY = "${NAMESPACE}registeredBy"

    /** The application used to create this registration. */
    const val REGISTERED_WITH = "${NAMESPACE}registeredWith"

    /** Timestamp when the registration was created (xsd:dateTime). */
    const val REGISTERED_AT = "${NAMESPACE}registeredAt"

    /** Timestamp when the registration was last updated (xsd:dateTime). */
    const val UPDATED_AT = "${NAMESPACE}updatedAt"

    /** The agent (app or social agent) that was registered. */
    const val REGISTERED_AGENT = "${NAMESPACE}registeredAgent"

    /** The shape tree that describes the registered data type. */
    const val REGISTERED_SHAPE_TREE = "${NAMESPACE}registeredShapeTree"

    //Application description predicates
    /** Human-readable name of the application. */
    const val APPLICATION_NAME = "${NAMESPACE}applicationName"

    /** Human-readable description of the application's function. */
    const val APPLICATION_DESCRIPTION = "${NAMESPACE}applicationDescription"

    /** The social agent who authored this application. */
    const val APPLICATION_AUTHOR = "${NAMESPACE}applicationAuthor"

    /** URI of a thumbnail image for the application. */
    const val APPLICATION_THUMBNAIL = "${NAMESPACE}applicationThumbnail"

    /** URI the authorization agent redirects to after authorization. */
    const val HAS_AUTHORIZATION_CALLBACK_ENDPOINT = "${NAMESPACE}hasAuthorizationCallbackEndpoint"

    /** URI the authorization agent uses for authorization redirects. */
    const val HAS_AUTHORIZATION_REDIRECT_ENDPOINT = "${NAMESPACE}hasAuthorizationRedirectEndpoint"

    //Access need predicates
    /** Links an access need group to its constituent access needs. */
    const val HAS_ACCESS_NEED = "${NAMESPACE}hasAccessNeed"

    /** Links an authorization or grant to its access need groups. */
    const val HAS_ACCESS_NEED_GROUP = "${NAMESPACE}hasAccessNeedGroup"

    /** Marks an access need as required (`interop:Required`) or optional (`interop:Optional`). */
    const val ACCESS_NECESSITY = "${NAMESPACE}accessNecessity"

    /** Context for presenting the access need (`interop:PersonalAccess`, `interop:SharedAccess`). */
    const val ACCESS_SCENARIO = "${NAMESPACE}accessScenario"

    /** The requested access modes (e.g. `acl:Read`, `acl:Write`). */
    const val ACCESS_MODE = "${NAMESPACE}accessMode"

    /** Additional access modes granted to the data creator. */
    const val CREATOR_ACCESS_MODE = "${NAMESPACE}creatorAccessMode"

    /** References a parent access need this need inherits from. */
    const val INHERITS_FROM_NEED = "${NAMESPACE}inheritsFromNeed"

    //Authorization / grant predicates
    /** Links a social agent registration or access grant to an access grant. */
    const val HAS_ACCESS_GRANT = "${NAMESPACE}hasAccessGrant"

    /** The social agent who granted the authorization. */
    const val GRANTED_BY = "${NAMESPACE}grantedBy"

    /** The application used to perform the authorization. */
    const val GRANTED_WITH = "${NAMESPACE}grantedWith"

    /** Timestamp when the authorization was granted (xsd:dateTime). */
    const val GRANTED_AT = "${NAMESPACE}grantedAt"

    /** The agent receiving the authorization or grant. */
    const val GRANTEE = "${NAMESPACE}grantee"

    /** The social agent who owns the authorized data. */
    const val DATA_OWNER = "${NAMESPACE}dataOwner"

    /** Scope of the authorization (`All`, `AllFromAgent`, `AllFromRegistry`, `SelectedFromRegistry`, `Inherited`). */
    const val SCOPE_OF_AUTHORIZATION = "${NAMESPACE}scopeOfAuthorization"

    /** Scope of the grant (mirrors scopeOfAuthorization for the issued grant). */
    const val SCOPE_OF_GRANT = "${NAMESPACE}scopeOfGrant"

    /** Links a data grant to the access need it satisfies. */
    const val SATISFIES_ACCESS_NEED = "${NAMESPACE}satisfiesAccessNeed"

    /** References a parent authorization this authorization inherits from. */
    const val INHERITS_FROM_AUTHORIZATION = "${NAMESPACE}inheritsFromAuthorization"

    /** References a parent data grant this grant inherits from. */
    const val INHERITS_FROM_GRANT = "${NAMESPACE}inheritsFromGrant"

    /** The data grant this delegated grant re-shares. */
    const val DELEGATION_OF_GRANT = "${NAMESPACE}delegationOfGrant"

    /** References the mutual registration in the other agent's registry. */
    const val RECIPROCAL_REGISTRATION = "${NAMESPACE}reciprocalRegistration"

    /** A secure URL for accepting a social agent invitation. */
    const val CAPABILITY_URL = "${NAMESPACE}capabilityUrl"

    //Access necessity named individuals
    /** Marks an access need as required. */
    const val REQUIRED = "${NAMESPACE}Required"

    /** Marks an access need as optional. */
    const val OPTIONAL = "${NAMESPACE}Optional"

    //Access scenario named individuals
    /** Access for the agent's own personal data. */
    const val PERSONAL_ACCESS = "${NAMESPACE}PersonalAccess"

    /** Access shared with or from other agents. */
    const val SHARED_ACCESS = "${NAMESPACE}SharedAccess"
}
