package com.pondersource.shared.vocab

/**
 * Activity Streams 2.0 vocabulary constants.
 * https://www.w3.org/ns/activitystreams#
 * Used by the Solid Notifications Protocol for notification message types.
 * Spec: https://solidproject.org/TR/notifications-protocol
 */
object AS {
    const val NAMESPACE = "https://www.w3.org/ns/activitystreams#"

    //Activity types (notification event types) 
    const val CREATE = "${NAMESPACE}Create"
    const val UPDATE = "${NAMESPACE}Update"
    const val DELETE = "${NAMESPACE}Delete"
    const val ADD = "${NAMESPACE}Add"
    const val REMOVE = "${NAMESPACE}Remove"
    const val MOVE = "${NAMESPACE}Move"
    const val ANNOUNCE = "${NAMESPACE}Announce"

    //Object types
    const val OBJECT_TYPE = "${NAMESPACE}Object"
    const val ACTIVITY = "${NAMESPACE}Activity"
    const val COLLECTION = "${NAMESPACE}Collection"
    const val ORDERED_COLLECTION = "${NAMESPACE}OrderedCollection"
    const val LINK = "${NAMESPACE}Link"

    // Predicates
    /** The object affected by the activity. */
    const val OBJECT = "${NAMESPACE}object"

    /** Target resource(s) for the activity. */
    const val TARGET = "${NAMESPACE}target"

    /** When the notification was published (xsd:dateTime). */
    const val PUBLISHED = "${NAMESPACE}published"

    /** Unique identifier for the notification. */
    const val ID = "${NAMESPACE}id"

    /** Agent that performed the activity. */
    const val ACTOR = "${NAMESPACE}actor"

    /** Summary of the activity. */
    const val SUMMARY = "${NAMESPACE}summary"

    const val ITEMS = "${NAMESPACE}items"
    const val ORDERED_ITEMS = "${NAMESPACE}orderedItems"
    const val FIRST = "${NAMESPACE}first"
    const val LAST = "${NAMESPACE}last"
    const val NEXT = "${NAMESPACE}next"
    const val PREV = "${NAMESPACE}prev"
    const val TOTAL_ITEMS = "${NAMESPACE}totalItems"
}
