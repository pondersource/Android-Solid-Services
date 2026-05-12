package com.erfangholami.androidsolidservices.shared.domain.sharing

import android.os.Parcelable
import com.erfangholami.androidsolidservices.shared.vocab.Solid
import kotlinx.parcelize.Parcelize

/**
 * Identifies the recipient of a share.
 *
 * - [WebIdReceiver]  — a single Solid user identified by WebID.
 * - [GroupReceiver]  — a `vcard:Group` whose members are granted access.
 * - [Public]         — any agent (`foaf:Agent`); resource is reachable by URL.
 */
public sealed class ShareReceiver : Parcelable {

    public abstract fun toRdfSubject(): String

    @Parcelize
    public data class WebIdReceiver(val webId: String) : ShareReceiver() {
        override fun toRdfSubject(): String = webId
    }

    @Parcelize
    public data class GroupReceiver(val groupUri: String) : ShareReceiver() {
        override fun toRdfSubject(): String = groupUri
    }

    @Parcelize
    public data object Public : ShareReceiver() {
        override fun toRdfSubject(): String = Solid.PUBLIC_AGENT
    }

    public fun kind(): Int = when (this) {
        is WebIdReceiver -> KIND_WEBID
        is GroupReceiver -> KIND_GROUP
        is Public -> KIND_PUBLIC
    }

    public fun value(): String? = when (this) {
        is WebIdReceiver -> webId
        is GroupReceiver -> groupUri
        is Public -> null
    }

    public companion object {
        public const val KIND_WEBID: Int = 0
        public const val KIND_GROUP: Int = 1
        public const val KIND_PUBLIC: Int = 2

        /**
         * Reverses [toRdfSubject]. The kind hint distinguishes a WebID from a group URI
         * since both are arbitrary IRIs in RDF.
         */
        public fun from(rdfSubject: String, isGroup: Boolean = false): ShareReceiver = when {
            rdfSubject == Solid.PUBLIC_AGENT -> Public
            isGroup -> GroupReceiver(rdfSubject)
            else -> WebIdReceiver(rdfSubject)
        }

        /** Reconstructs a receiver from its AIDL `(kind, value)` representation. */
        public fun fromKind(kind: Int, value: String?): ShareReceiver = when (kind) {
            KIND_WEBID -> WebIdReceiver(value ?: error("WebID value is required"))
            KIND_GROUP -> GroupReceiver(value ?: error("Group URI is required"))
            KIND_PUBLIC -> Public
            else -> error("Unknown receiver kind: $kind")
        }
    }
}
