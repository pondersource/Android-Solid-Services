package com.erfangholami.androidsolidservices.shared.domain.sharing

/**
 * Pod-relative paths used by the sharing feature.
 *
 * - [SHARES_CONTAINER_NAME] holds private bookkeeping (given/received indexes).
 *   Owner-only ACL.
 * - [PROFILES_CONTAINER_NAME] holds publicly-resolvable artifacts (profile snapshots
 *   and HTML business cards). Each artifact has its own per-resource ACL.
 */
public const val SHARES_CONTAINER_NAME: String = ".shares/"
public const val PROFILES_CONTAINER_NAME: String = "solidshare/profiles/"

public const val GIVEN_SHARES_FILE_NAME: String = "given_shares.ttl"
public const val RECEIVED_SHARES_FILE_NAME: String = "received_shares.ttl"

/** Custom URI scheme that opens scanned shares directly in the SolidShare app. */
public const val SOLID_SHARE_URI_SCHEME: String = "solidshare"
