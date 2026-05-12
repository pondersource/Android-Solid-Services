package com.erfangholami.androidsolidservices.shared.vocab

/**
 * POSIX stat vocabulary constants used by Solid servers for resource metadata.
 * http://www.w3.org/ns/posix/stat#
 * Referenced in: https://solidproject.org/TR/protocol (container metadata)
 */
public object STAT {
    public const val NAMESPACE: String = "http://www.w3.org/ns/posix/stat#"

    /** File size in bytes. Servers SHOULD include this for contained resources. */
    public const val SIZE: String = "${NAMESPACE}size"

    /** Unix timestamp (seconds since epoch) of last modification. */
    public const val MTIME: String = "${NAMESPACE}mtime"

    /** Unix timestamp of creation. */
    public const val CTIME: String = "${NAMESPACE}ctime"

    /** Octal permission bits. */
    public const val MODE: String = "${NAMESPACE}mode"
}
