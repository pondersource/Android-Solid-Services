package com.pondersource.shared.vocab

/**
 * Personal Information Management (PIM) space vocabulary.
 * http://www.w3.org/ns/pim/space#
 * Used by Solid for storage and preferences.
 */
object PIM {
    const val NAMESPACE = "http://www.w3.org/ns/pim/space#"

    //Types
    /** A Solid pod storage root container. */
    const val STORAGE_TYPE = "${NAMESPACE}Storage"

    /** A configuration / preferences file. */
    const val CONFIGURATION_FILE = "${NAMESPACE}ConfigurationFile"

    /** A workspace for personal data. */
    const val WORKSPACE_TYPE = "${NAMESPACE}Workspace"

    const val CONTROLLED_STORAGE = "${NAMESPACE}ControlledStorage"
    const val PERSONAL_STORAGE = "${NAMESPACE}PersonalStorage"

    //Predicates
    /** Links a WebID to its storage root(s). */
    const val STORAGE = "${NAMESPACE}storage"

    /** Links a WebID to its preferences / configuration file. */
    const val PREFERENCES_FILE = "${NAMESPACE}preferencesFile"

    const val WORKSPACE = "${NAMESPACE}workspace"
    const val MASTER_WORKSPACE = "${NAMESPACE}masterWorkspace"
}
