package com.pondersource.shared.vocab

/**
 * Solid Notifications Protocol vocabulary constants.
 * http://www.w3.org/ns/solid/notifications#
 * Spec: https://solidproject.org/TR/notifications-protocol
 */
object Notify {
    const val NAMESPACE = "http://www.w3.org/ns/solid/notifications#"

    //Channel types
    const val WEB_SOCKET_CHANNEL_2023 = "${NAMESPACE}WebSocketChannel2023"
    const val EVENT_SOURCE_CHANNEL_2023 = "${NAMESPACE}EventSourceChannel2023"
    const val WEBHOOK_CHANNEL_2023 = "${NAMESPACE}WebhookChannel2023"
    const val STREAMING_HTTP_CHANNEL_2023 = "${NAMESPACE}StreamingHTTPChannel2023"
    const val LDN_CHANNEL_2023 = "${NAMESPACE}LDNChannel2023"

    //Subscription predicates
    /** The resource being subscribed to. */
    const val TOPIC = "${NAMESPACE}topic"

    /** The channel type IRI. */
    const val CHANNEL_TYPE = "${NAMESPACE}channelType"

    /** Send notifications to this endpoint (push/webhook pattern). */
    const val SEND_TO = "${NAMESPACE}sendTo"

    /** Receive notifications from this endpoint (pull/SSE/WS pattern). */
    const val RECEIVE_FROM = "${NAMESPACE}receiveFrom"

    //Channel feature predicates
    /** Start sending notifications after this datetime (xsd:dateTime). */
    const val START_AT = "${NAMESPACE}startAt"

    /** Stop sending notifications after this datetime (xsd:dateTime). */
    const val END_AT = "${NAMESPACE}endAt"

    /** Last known state of the resource (xsd:string). */
    const val STATE = "${NAMESPACE}state"

    /** Minimum duration between notifications (xsd:duration). */
    const val RATE = "${NAMESPACE}rate"

    /** Accepted media types for notifications. */
    const val ACCEPT = "${NAMESPACE}accept"

    //Notification message predicates
    /** The last known ETag / state of the resource at time of notification. */
    const val LAST_KNOWN_STATE = "${NAMESPACE}lastKnownState"
}
