package com.pondersource.shared.vocab

/**
 * Solid Terms vocabulary constants.
 * http://www.w3.org/ns/solid/terms#
 */
object Solid {
    const val NAMESPACE = "http://www.w3.org/ns/solid/terms#"

    //Authentication
    /** OpenID Connect issuer for a WebID. */
    const val OIDC_ISSUER = "${NAMESPACE}oidcIssuer"

    /** Client identifier registration document. */
    const val OIDC_REGISTRATION = "${NAMESPACE}oidcRegistration"

    //Storage
    /** Links a resource to a storage description resource. */
    const val STORAGE_DESCRIPTION = "${NAMESPACE}storageDescription"

    /** Identifies the owner of a storage. */
    const val OWNER = "${NAMESPACE}owner"

    //Type Index
    const val PRIVATE_TYPE_INDEX = "${NAMESPACE}privateTypeIndex"
    const val PUBLIC_TYPE_INDEX = "${NAMESPACE}publicTypeIndex"
    const val TYPE_REGISTRATION = "${NAMESPACE}TypeRegistration"
    const val FOR_CLASS = "${NAMESPACE}forClass"
    const val INSTANCE = "${NAMESPACE}instance"
    const val INSTANCE_CONTAINER = "${NAMESPACE}instanceContainer"
    const val TYPE_INDEX = "${NAMESPACE}TypeIndex"
    const val UNLISTED_DOCUMENT = "${NAMESPACE}UnlistedDocument"
    const val LISTED_DOCUMENT = "${NAMESPACE}ListedDocument"

    //N3 Patch
    /** rdf:type for a Solid N3 Patch document. */
    const val INSERT_DELETE_PATCH = "${NAMESPACE}InsertDeletePatch"

    /** Triples to delete in a patch. */
    const val DELETES = "${NAMESPACE}deletes"

    /** Triples to insert in a patch. */
    const val INSERTS = "${NAMESPACE}inserts"

    /** Conditions that must hold for the patch to apply. */
    const val WHERE = "${NAMESPACE}where"

    //Access Control (Solid-specific agent classes)
    /** Matches any agent (authenticated or not). Same as foaf:Agent. */
    const val PUBLIC_AGENT = "http://xmlns.com/foaf/0.1/Agent"

    /** Matches only authenticated agents. */
    const val AUTHENTICATED_AGENT = "${NAMESPACE}AuthenticatedAgent"

    // Notifications
    const val NOTIFICATION_CHANNEL = "${NAMESPACE}notificationChannel"
}
