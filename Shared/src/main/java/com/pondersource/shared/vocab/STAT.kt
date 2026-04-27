package com.pondersource.shared.vocab

/**
 * POSIX stat vocabulary constants used by Solid servers for resource metadata.
 * http://www.w3.org/ns/posix/stat#
 * Referenced in: https://solidproject.org/TR/protocol (container metadata)
 */
object STAT {
    const val NAMESPACE = "http://www.w3.org/ns/posix/stat#"

    /** File size in bytes. Servers SHOULD include this for contained resources. */
    const val SIZE = "${NAMESPACE}size"

    /** Unix timestamp (seconds since epoch) of last modification. */
    const val MTIME = "${NAMESPACE}mtime"

    /** Unix timestamp of creation. */
    const val CTIME = "${NAMESPACE}ctime"

    /** Octal permission bits. */
    const val MODE = "${NAMESPACE}mode"
}
