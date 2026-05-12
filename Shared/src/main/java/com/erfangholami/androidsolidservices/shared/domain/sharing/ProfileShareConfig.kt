package com.erfangholami.androidsolidservices.shared.domain.sharing

/**
 * User-supplied configuration for creating a profile share.
 *
 * The owner picks which [selectedFields] to include from their WebID profile and
 * who the [receiver] is. The resulting snapshot lives at a fresh URI on the owner's
 * pod and is added to `given_shares.ttl`.
 */
public data class ProfileShareConfig(
    val selectedFields: Set<ProfileField>,
    val receiver: ShareReceiver,
)
