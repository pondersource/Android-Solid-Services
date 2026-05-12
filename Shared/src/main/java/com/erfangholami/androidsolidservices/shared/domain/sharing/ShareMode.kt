package com.erfangholami.androidsolidservices.shared.domain.sharing

import com.erfangholami.androidsolidservices.shared.vocab.ACL

/**
 * Access mode granted to the receiver of a share.
 *
 * Maps 1:1 to a WAC `acl:mode` predicate.
 */
public enum class ShareMode {
    READ,
    APPEND,
    WRITE;

    public fun toAclPredicate(): String = when (this) {
        READ -> ACL.READ
        APPEND -> ACL.APPEND
        WRITE -> ACL.WRITE
    }

    public companion object {
        public fun fromAclPredicate(predicate: String): ShareMode? = when (predicate) {
            ACL.READ -> READ
            ACL.APPEND -> APPEND
            ACL.WRITE -> WRITE
            else -> null
        }

        /**
         * Picks the most permissive mode from a set of WAC predicates.
         * Write > Append > Read.
         */
        public fun strongest(predicates: Set<String>): ShareMode? = when {
            predicates.contains(ACL.WRITE) -> WRITE
            predicates.contains(ACL.APPEND) -> APPEND
            predicates.contains(ACL.READ) -> READ
            else -> null
        }
    }
}
