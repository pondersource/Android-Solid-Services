package com.erfangholami.androidsolidservices.api.sharing.implementation

import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.access.AclAuthorization
import com.erfangholami.androidsolidservices.shared.domain.access.SolidACLResource
import com.erfangholami.androidsolidservices.shared.domain.crud.N3Patch
import com.erfangholami.androidsolidservices.shared.domain.network.SolidNetworkResponse
import com.erfangholami.androidsolidservices.shared.domain.profile.WebId
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidContainer
import com.erfangholami.androidsolidservices.shared.domain.sharing.GIVEN_SHARES_FILE_NAME
import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.PROFILES_CONTAINER_NAME
import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.RECEIVED_SHARES_FILE_NAME
import com.erfangholami.androidsolidservices.shared.domain.sharing.SHARES_CONTAINER_NAME
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareMode
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareReceiver
import com.erfangholami.androidsolidservices.shared.domain.sharing.rdf.GivenSharesIndexRDF
import com.erfangholami.androidsolidservices.shared.domain.sharing.rdf.ReceivedSharesIndexRDF
import com.erfangholami.androidsolidservices.shared.vocab.ACL
import com.erfangholami.androidsolidservices.api.auth.Authenticator
import com.erfangholami.androidsolidservices.api.resource.SolidResourceManager
import java.net.URI
import java.util.UUID

/**
 * Low-level helpers for the sharing pipeline:
 *
 * - pod-root resolution
 * - container & index file bootstrap
 * - WAC ACL read / modify / write
 * - N3-Patch driven updates of the given/received indexes
 *
 * All methods either succeed or throw — callers wrap them in
 * [SolidNetworkResponse] at the public boundary.
 */
internal class SharingManagerHelper {

    companion object {
        @Volatile
        private var INSTANCE: SharingManagerHelper? = null

        fun getInstance(authenticator: Authenticator): SharingManagerHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharingManagerHelper(SolidResourceManager.getInstance(authenticator))
                    .also { INSTANCE = it }
            }

        fun getInstance(resourceManager: SolidResourceManager): SharingManagerHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharingManagerHelper(resourceManager).also { INSTANCE = it }
            }
    }

    val rm: SolidResourceManager

    private constructor(resourceManager: SolidResourceManager) {
        this.rm = resourceManager
    }

    // ── Pod root / paths ────────────────────────────────────────────────────

    suspend fun getPodRoot(webId: String): URI {
        val profile = rm.read(webId, URI.create(webId), WebId::class.java).getOrThrow()
        val storage = profile.getStorages().firstOrNull()
            ?: error("WebID profile has no pim:storage entry")
        return URI.create(storage.toString().ensureTrailingSlash())
    }

    fun givenSharesUri(podRoot: URI): URI =
        URI.create("${podRoot}${SHARES_CONTAINER_NAME}${GIVEN_SHARES_FILE_NAME}")

    fun receivedSharesUri(podRoot: URI): URI =
        URI.create("${podRoot}${SHARES_CONTAINER_NAME}${RECEIVED_SHARES_FILE_NAME}")

    fun sharesContainerUri(podRoot: URI): URI =
        URI.create("${podRoot}${SHARES_CONTAINER_NAME}")

    fun profilesContainerUri(podRoot: URI): URI =
        URI.create("${podRoot}${PROFILES_CONTAINER_NAME}")

    fun newProfileSnapshotUri(podRoot: URI): URI =
        URI.create("${podRoot}${PROFILES_CONTAINER_NAME}${UUID.randomUUID()}.ttl")

    fun htmlSiblingOf(snapshotUri: URI): URI {
        val s = snapshotUri.toString()
        val withoutExt = if (s.endsWith(".ttl")) s.dropLast(4) else s
        return URI.create("$withoutExt.html")
    }

    // ── Container bootstrap ─────────────────────────────────────────────────

    /**
     * Ensures `{podRoot}/.shares/` exists with an owner-only ACL, and that
     * both index files exist (created empty if missing).
     */
    suspend fun ensurePrivateSharesContainer(webId: String, podRoot: URI) {
        val containerUri = sharesContainerUri(podRoot)
        ensureContainer(webId, containerUri)
        ensureOwnerOnlyAclFor(webId, containerUri, isContainer = true)
        ensureEmptyRdf(webId, givenSharesUri(podRoot))
        ensureEmptyRdf(webId, receivedSharesUri(podRoot))
    }

    /**
     * Ensures `{podRoot}/solidshare/profiles/` exists. Per-resource ACLs are
     * set on each individual snapshot; the container itself stays
     * owner-controlled (children with their own ACL override it).
     */
    suspend fun ensureProfilesContainer(webId: String, podRoot: URI) {
        val containerUri = profilesContainerUri(podRoot)
        ensureContainer(webId, containerUri)
    }

    private suspend fun ensureContainer(webId: String, containerUri: URI) {
        val head = rm.head(webId, containerUri)
        if (head is SolidNetworkResponse.Success) return
        rm.create(webId, SolidContainer(containerUri)).getOrThrow()
    }

    private suspend fun ensureEmptyRdf(webId: String, uri: URI) {
        val head = rm.head(webId, uri)
        if (head is SolidNetworkResponse.Success) return
        rm.create(
            webId,
            GivenSharesIndexRDF(
                identifier = uri,
                mediaType = MediaType.JSON_LD,
                quads = null,
                headers = null,
            ),
        ).getOrThrow()
    }

    // ── ACL: read / write / mutate ──────────────────────────────────────────

    suspend fun readAcl(webId: String, resourceUri: URI): Pair<URI, SolidACLResource> {
        val metadata = rm.head(webId, resourceUri).getOrThrow()
        val aclUri = metadata.aclUri
            ?: error("Resource $resourceUri does not advertise an acl link")

        val existing = rm.head(webId, aclUri)
        return if (existing is SolidNetworkResponse.Success) {
            aclUri to rm.read(webId, aclUri, SolidACLResource::class.java).getOrThrow()
        } else {
            // ACL not yet materialized (inheriting from container) — start fresh.
            aclUri to SolidACLResource(aclUri)
        }
    }

    suspend fun writeAcl(webId: String, aclUri: URI, acl: SolidACLResource) {
        // PUT replaces; that's the only safe way to commit a multi-rule ACL
        // because there is no standard PATCH for ACL resources.
        val toWrite = SolidACLResource(
            identifier = aclUri,
            mediaType = MediaType.JSON_LD,
            quads = acl.getAllQuads(),
            headers = null,
        )
        val head = rm.head(webId, aclUri)
        if (head is SolidNetworkResponse.Success) {
            rm.update(webId, toWrite).getOrThrow()
        } else {
            rm.create(webId, toWrite).getOrThrow()
        }
    }

    /**
     * Builds an ACL that grants the owner full Read/Write/Control on
     * [targetUri] (or on the container's children, if [isContainer]) and
     * commits it.
     */
    suspend fun ensureOwnerOnlyAclFor(
        webId: String,
        targetUri: URI,
        isContainer: Boolean,
    ) {
        val (aclUri, acl) = readAcl(webId, targetUri)
        val alreadyHasOwnerRule = acl.getAuthorizations().any { auth ->
            auth.agents.any { it.toString() == webId } &&
                    auth.modes.containsAll(setOf(ACL.READ, ACL.WRITE, ACL.CONTROL))
        }
        if (alreadyHasOwnerRule) return

        acl.addAuthorization(
            AclAuthorization(
                subject = "${aclUri}#owner",
                accessTo = if (isContainer) emptyList() else listOf(targetUri),
                default = if (isContainer) listOf(targetUri) else emptyList(),
                modes = setOf(ACL.READ, ACL.WRITE, ACL.CONTROL),
                agents = listOf(URI.create(webId)),
            ),
        )
        writeAcl(webId, aclUri, acl)
    }

    /**
     * Adds an authorization granting [mode] to [receiver] on [resourceUri] and
     * commits the ACL. Replaces any existing rule for the same (resource, receiver).
     */
    suspend fun grantAccess(
        webId: String,
        resourceUri: URI,
        mode: ShareMode,
        receiver: ShareReceiver,
    ) {
        val (aclUri, acl) = readAcl(webId, resourceUri)

        // Make sure the owner keeps full control even on first ACL write.
        val ownerAuth = AclAuthorization(
            subject = "${aclUri}#owner",
            accessTo = listOf(resourceUri),
            modes = setOf(ACL.READ, ACL.WRITE, ACL.CONTROL),
            agents = listOf(URI.create(webId)),
        )

        val keep = acl.getAuthorizations().filterNot { auth ->
            ruleMatches(auth, resourceUri, receiver) ||
                    auth.agents.any { it.toString() == webId } && auth.modes.containsAll(
                setOf(ACL.READ, ACL.WRITE, ACL.CONTROL),
            )
        }

        val grant = AclAuthorization(
            subject = "${aclUri}#share-${UUID.randomUUID()}",
            accessTo = listOf(resourceUri),
            modes = setOf(mode.toAclPredicate()),
            agents = (receiver as? ShareReceiver.WebIdReceiver)?.let { listOf(URI.create(it.webId)) }
                ?: emptyList(),
            agentClasses = if (receiver is ShareReceiver.Public) {
                listOf(URI.create(ShareReceiver.Public.toRdfSubject()))
            } else emptyList(),
            agentGroups = (receiver as? ShareReceiver.GroupReceiver)?.let {
                listOf(URI.create(it.groupUri))
            } ?: emptyList(),
        )

        val rebuilt = SolidACLResource(aclUri)
        keep.forEach { rebuilt.addAuthorization(it) }
        rebuilt.addAuthorization(ownerAuth)
        rebuilt.addAuthorization(grant)
        writeAcl(webId, aclUri, rebuilt)
    }

    /**
     * Removes any authorization on [resourceUri] that targets [receiver] and
     * commits the ACL.
     */
    suspend fun revokeAccess(
        webId: String,
        resourceUri: URI,
        receiver: ShareReceiver,
    ) {
        val (aclUri, acl) = readAcl(webId, resourceUri)
        val ownerAuth = AclAuthorization(
            subject = "${aclUri}#owner",
            accessTo = listOf(resourceUri),
            modes = setOf(ACL.READ, ACL.WRITE, ACL.CONTROL),
            agents = listOf(URI.create(webId)),
        )
        val keep = acl.getAuthorizations().filterNot { auth ->
            ruleMatches(auth, resourceUri, receiver) ||
                    auth.agents.any { it.toString() == webId } && auth.modes.containsAll(
                setOf(ACL.READ, ACL.WRITE, ACL.CONTROL),
            )
        }
        val rebuilt = SolidACLResource(aclUri)
        keep.forEach { rebuilt.addAuthorization(it) }
        rebuilt.addAuthorization(ownerAuth)
        writeAcl(webId, aclUri, rebuilt)
    }

    private fun ruleMatches(
        auth: AclAuthorization,
        resourceUri: URI,
        receiver: ShareReceiver,
    ): Boolean {
        val touchesResource =
            auth.accessTo.any { it == resourceUri } || auth.default.any { it == resourceUri }
        if (!touchesResource) return false
        return when (receiver) {
            is ShareReceiver.WebIdReceiver ->
                auth.agents.any { it.toString() == receiver.webId }

            is ShareReceiver.GroupReceiver ->
                auth.agentGroups.any { it.toString() == receiver.groupUri }

            is ShareReceiver.Public ->
                auth.agentClasses.any { it.toString() == ShareReceiver.Public.toRdfSubject() }
        }
    }

    // ── Index file: read / mutate via N3 Patch ──────────────────────────────

    suspend fun readGivenIndex(webId: String, podRoot: URI): GivenSharesIndexRDF {
        val uri = givenSharesUri(podRoot)
        return rm.read(webId, uri, GivenSharesIndexRDF::class.java).getOrThrow()
    }

    suspend fun readReceivedIndex(webId: String, podRoot: URI): ReceivedSharesIndexRDF {
        val uri = receivedSharesUri(podRoot)
        return rm.read(webId, uri, ReceivedSharesIndexRDF::class.java).getOrThrow()
    }

    suspend fun replaceGivenShare(webId: String, podRoot: URI, share: GivenShare) {
        val uri = givenSharesUri(podRoot)
        // Replace any (subject == receiver, object == resourceUri) triple regardless of mode,
        // then insert the new one. We need a separate patch for delete to handle the case
        // where no matching triple exists (where-clause would fail otherwise).
        val receiverIri = share.receiver.toRdfSubject()
        val current = readGivenIndex(webId, podRoot).getShares()
        val existing = current.firstOrNull {
            it.receiver.toRdfSubject() == receiverIri && it.resourceUri == share.resourceUri
        }
        if (existing != null) {
            rm.patch(
                webId, uri,
                N3Patch.build {
                    delete(receiverIri, existing.mode.toAclPredicate(), share.resourceUri)
                },
            ).getOrThrow()
        }
        rm.patch(
            webId, uri,
            N3Patch.build {
                insert(receiverIri, share.mode.toAclPredicate(), share.resourceUri)
            },
        ).getOrThrow()
    }

    suspend fun removeGivenShare(
        webId: String,
        podRoot: URI,
        resourceUri: String,
        receiver: ShareReceiver,
    ) {
        val uri = givenSharesUri(podRoot)
        val receiverIri = receiver.toRdfSubject()
        val current = readGivenIndex(webId, podRoot).getShares()
        val existing = current.firstOrNull {
            it.receiver.toRdfSubject() == receiverIri && it.resourceUri == resourceUri
        } ?: return
        rm.patch(
            webId, uri,
            N3Patch.build {
                delete(receiverIri, existing.mode.toAclPredicate(), resourceUri)
            },
        ).getOrThrow()
    }

    suspend fun replaceReceivedShare(webId: String, podRoot: URI, share: ReceivedShare) {
        val uri = receivedSharesUri(podRoot)
        val current = readReceivedIndex(webId, podRoot).getShares()
        val existing = current.firstOrNull {
            it.ownerWebId == share.ownerWebId && it.resourceUri == share.resourceUri
        }
        if (existing != null) {
            rm.patch(
                webId, uri,
                N3Patch.build {
                    delete(share.ownerWebId, existing.mode.toAclPredicate(), share.resourceUri)
                },
            ).getOrThrow()
        }
        rm.patch(
            webId, uri,
            N3Patch.build {
                insert(share.ownerWebId, share.mode.toAclPredicate(), share.resourceUri)
            },
        ).getOrThrow()
    }

    suspend fun removeReceivedShare(
        webId: String,
        podRoot: URI,
        resourceUri: String,
        ownerWebId: String,
    ) {
        val uri = receivedSharesUri(podRoot)
        val current = readReceivedIndex(webId, podRoot).getShares()
        val existing = current.firstOrNull {
            it.ownerWebId == ownerWebId && it.resourceUri == resourceUri
        } ?: return
        rm.patch(
            webId, uri,
            N3Patch.build {
                delete(ownerWebId, existing.mode.toAclPredicate(), resourceUri)
            },
        ).getOrThrow()
    }

    // ── Read-back helpers ───────────────────────────────────────────────────

    /**
     * Reads the resource's ACL and returns every share-like rule found
     * (one [GivenShare] per receiver × mode pair).
     */
    suspend fun getSharesFromAcl(webId: String, resourceUri: URI): List<GivenShare> {
        val (_, acl) = readAcl(webId, resourceUri)
        val shares = mutableListOf<GivenShare>()
        acl.getAuthorizations().forEach { auth ->
            val applies =
                auth.accessTo.any { it == resourceUri } || auth.default.any { it == resourceUri }
            if (!applies) return@forEach
            val mode = ShareMode.strongest(auth.modes) ?: return@forEach
            // Skip the owner's own self-rule.
            val ownerSelf = auth.agents.size == 1 &&
                    auth.agents.first().toString() == webId &&
                    auth.modes.contains(ACL.CONTROL)
            if (ownerSelf) return@forEach

            auth.agents.forEach { agent ->
                if (agent.toString() != webId) {
                    shares += GivenShare(
                        receiver = ShareReceiver.WebIdReceiver(agent.toString()),
                        mode = mode,
                        resourceUri = resourceUri.toString(),
                    )
                }
            }
            auth.agentGroups.forEach { g ->
                shares += GivenShare(
                    receiver = ShareReceiver.GroupReceiver(g.toString()),
                    mode = mode,
                    resourceUri = resourceUri.toString(),
                )
            }
            auth.agentClasses.forEach { c ->
                if (c.toString() == ShareReceiver.Public.toRdfSubject()) {
                    shares += GivenShare(
                        receiver = ShareReceiver.Public,
                        mode = mode,
                        resourceUri = resourceUri.toString(),
                    )
                }
            }
        }
        return shares.distinct()
    }

    /**
     * HEADs [resourceUri] from the receiver's perspective and returns the
     * strongest mode the caller is allowed plus the `solid:owner` link, if any.
     */
    suspend fun probeReceivedAccess(
        webId: String,
        resourceUri: URI,
    ): Pair<ShareMode, String?>? {
        val metadata = rm.head(webId, resourceUri).getOrThrow()
        val wac = metadata.wacAllow ?: return null
        val combined = (wac.userModes + wac.publicModes)
        val mode = when {
            combined.contains("write") -> ShareMode.WRITE
            combined.contains("append") -> ShareMode.APPEND
            combined.contains("read") -> ShareMode.READ
            else -> return null
        }
        val owner = metadata.ownerUri?.toString()
        return mode to owner
    }
}

internal fun String.ensureTrailingSlash(): String =
    if (endsWith("/")) this else "$this/"
