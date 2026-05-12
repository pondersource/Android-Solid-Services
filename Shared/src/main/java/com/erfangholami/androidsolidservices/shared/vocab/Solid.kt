package com.erfangholami.androidsolidservices.shared.vocab

/**
 * Solid Terms vocabulary constants.
 * http://www.w3.org/ns/solid/terms#
 */
public object Solid {
    public const val NAMESPACE: String = "http://www.w3.org/ns/solid/terms#"

    //Authentication
    /** OpenID Connect issuer for a WebID. */
    public const val OIDC_ISSUER: String = "${NAMESPACE}oidcIssuer"

    /** Client identifier registration document. */
    public const val OIDC_REGISTRATION: String = "${NAMESPACE}oidcRegistration"

    //Storage
    /** Links a resource to a storage description resource. */
    public const val STORAGE_DESCRIPTION: String = "${NAMESPACE}storageDescription"

    /** Identifies the owner of a storage. */
    public const val OWNER: String = "${NAMESPACE}owner"

    //Type Index
    public const val PRIVATE_TYPE_INDEX: String = "${NAMESPACE}privateTypeIndex"
    public const val PUBLIC_TYPE_INDEX: String = "${NAMESPACE}publicTypeIndex"
    public const val TYPE_REGISTRATION: String = "${NAMESPACE}TypeRegistration"
    public const val FOR_CLASS: String = "${NAMESPACE}forClass"
    public const val INSTANCE: String = "${NAMESPACE}instance"
    public const val INSTANCE_CONTAINER: String = "${NAMESPACE}instanceContainer"
    public const val TYPE_INDEX: String = "${NAMESPACE}TypeIndex"
    public const val UNLISTED_DOCUMENT: String = "${NAMESPACE}UnlistedDocument"
    public const val LISTED_DOCUMENT: String = "${NAMESPACE}ListedDocument"

    //N3 Patch
    /** rdf:type for a Solid N3 Patch document. */
    public const val INSERT_DELETE_PATCH: String = "${NAMESPACE}InsertDeletePatch"

    /** Triples to delete in a patch. */
    public const val DELETES: String = "${NAMESPACE}deletes"

    /** Triples to insert in a patch. */
    public const val INSERTS: String = "${NAMESPACE}inserts"

    /** Conditions that must hold for the patch to apply. */
    public const val WHERE: String = "${NAMESPACE}where"

    //Access Control (Solid-specific agent classes)
    /** Matches any agent (authenticated or not). Same as foaf:Agent. */
    public const val PUBLIC_AGENT: String = "http://xmlns.com/foaf/0.1/Agent"

    /** Matches only authenticated agents. */
    public const val AUTHENTICATED_AGENT: String = "${NAMESPACE}AuthenticatedAgent"

    // Notifications
    public const val NOTIFICATION_CHANNEL: String = "${NAMESPACE}notificationChannel"
}
