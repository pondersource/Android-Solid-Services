package com.erfangholami.androidsolidservices.api.sharing.implementation

import android.net.Uri
import com.erfangholami.androidsolidservices.shared.domain.network.SolidNetworkResponse
import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.ProfileShareConfig
import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.SOLID_SHARE_URI_SCHEME
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareMode
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareReceiver
import com.erfangholami.androidsolidservices.api.auth.Authenticator
import com.erfangholami.androidsolidservices.api.resource.SolidResourceManager
import com.erfangholami.androidsolidservices.api.sharing.SharingManager
import java.net.URI

internal class SharingManagerImplementation : SharingManager {

    companion object {
        @Volatile
        private var INSTANCE: SharingManager? = null

        fun getInstance(authenticator: Authenticator): SharingManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharingManagerImplementation(authenticator).also { INSTANCE = it }
            }

        fun getInstance(resourceManager: SolidResourceManager): SharingManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharingManagerImplementation(resourceManager).also { INSTANCE = it }
            }
    }

    private val helper: SharingManagerHelper
    private val rm: SolidResourceManager
    private val profileBuilder: ProfileSnapshotBuilder

    private constructor(authenticator: Authenticator) {
        this.rm = SolidResourceManager.getInstance(authenticator)
        this.helper = SharingManagerHelper.getInstance(rm)
        this.profileBuilder = ProfileSnapshotBuilder(rm)
    }

    private constructor(resourceManager: SolidResourceManager) {
        this.rm = resourceManager
        this.helper = SharingManagerHelper.getInstance(rm)
        this.profileBuilder = ProfileSnapshotBuilder(rm)
    }

    // ── Given shares ────────────────────────────────────────────────────────

    override suspend fun getStoredGivenShares(
        webId: String,
    ): SolidNetworkResponse<List<GivenShare>> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        helper.readGivenIndex(webId, podRoot).getShares()
    }

    override suspend fun refreshGivenShares(
        webId: String,
    ): SolidNetworkResponse<List<GivenShare>> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        val stored = helper.readGivenIndex(webId, podRoot).getShares()
        // For each stored share, re-read the resource ACL and keep only the
        // (receiver, mode, resourceUri) tuples that still exist.
        val resourcesSeen = mutableSetOf<String>()
        val verified = mutableListOf<GivenShare>()
        stored.forEach { share ->
            if (resourcesSeen.add(share.resourceUri)) {
                val live = runCatching {
                    helper.getSharesFromAcl(webId, URI.create(share.resourceUri))
                }.getOrDefault(emptyList())
                verified += live
            }
        }
        // Re-sync the index: drop stale entries, add any not-yet-tracked ones.
        val staleByKey = stored.associateBy { it.shareKey() }.toMutableMap()
        verified.forEach { liveShare ->
            staleByKey.remove(liveShare.shareKey())
            helper.replaceGivenShare(webId, podRoot, liveShare)
        }
        staleByKey.values.forEach { stale ->
            helper.removeGivenShare(webId, podRoot, stale.resourceUri, stale.receiver)
        }
        verified.distinct()
    }

    override suspend fun getGivenSharesForResource(
        webId: String,
        resourceUri: String,
    ): SolidNetworkResponse<List<GivenShare>> = wrap {
        helper.getSharesFromAcl(webId, URI.create(resourceUri))
    }

    override suspend fun createShare(
        webId: String,
        resourceUri: String,
        mode: ShareMode,
        receiver: ShareReceiver,
    ): SolidNetworkResponse<GivenShare> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        val uri = URI.create(resourceUri)
        helper.grantAccess(webId, uri, mode, receiver)
        val share = GivenShare(receiver, mode, resourceUri)
        helper.replaceGivenShare(webId, podRoot, share)
        share
    }

    override suspend fun updateShare(
        webId: String,
        resourceUri: String,
        mode: ShareMode,
        receiver: ShareReceiver,
    ): SolidNetworkResponse<GivenShare> = createShare(webId, resourceUri, mode, receiver)

    override suspend fun revokeShare(
        webId: String,
        resourceUri: String,
        receiver: ShareReceiver,
    ): SolidNetworkResponse<Unit> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        helper.revokeAccess(webId, URI.create(resourceUri), receiver)
        helper.removeGivenShare(webId, podRoot, resourceUri, receiver)
    }

    // ── Profile share ───────────────────────────────────────────────────────

    override suspend fun createProfileShare(
        webId: String,
        config: ProfileShareConfig,
        mode: ShareMode,
    ): SolidNetworkResponse<GivenShare> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        helper.ensureProfilesContainer(webId, podRoot)

        val snapshotUri = helper.newProfileSnapshotUri(podRoot)
        val htmlUri = helper.htmlSiblingOf(snapshotUri)

        val snapshot = profileBuilder.build(webId, config, snapshotUri)
        rm.create(webId, snapshot).getOrThrow()
        helper.grantAccess(webId, snapshotUri, mode, config.receiver)

        val html = profileBuilder.buildHtml(webId, config, snapshot, htmlUri)
        rm.create(webId, html).getOrThrow()
        // Public HTML so non-Solid recipients can open the QR target in any browser.
        helper.grantAccess(webId, htmlUri, ShareMode.READ, ShareReceiver.Public)

        val share = GivenShare(
            receiver = config.receiver,
            mode = mode,
            resourceUri = snapshotUri.toString(),
        )
        helper.replaceGivenShare(webId, podRoot, share)
        share
    }

    // ── Received shares ─────────────────────────────────────────────────────

    override suspend fun getStoredReceivedShares(
        webId: String,
    ): SolidNetworkResponse<List<ReceivedShare>> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        helper.readReceivedIndex(webId, podRoot).getShares()
    }

    override suspend fun refreshReceivedShares(
        webId: String,
    ): SolidNetworkResponse<List<ReceivedShare>> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        val stored = helper.readReceivedIndex(webId, podRoot).getShares()
        val verified = mutableListOf<ReceivedShare>()
        stored.forEach { share ->
            val probe = runCatching {
                helper.probeReceivedAccess(webId, URI.create(share.resourceUri))
            }.getOrNull()
            if (probe != null) {
                val refreshed = ReceivedShare(
                    ownerWebId = probe.second ?: share.ownerWebId,
                    mode = probe.first,
                    resourceUri = share.resourceUri,
                )
                verified += refreshed
                helper.replaceReceivedShare(webId, podRoot, refreshed)
            } else {
                helper.removeReceivedShare(
                    webId, podRoot, share.resourceUri, share.ownerWebId,
                )
            }
        }
        verified
    }

    override suspend fun addReceivedShare(
        webId: String,
        resourceUri: String,
    ): SolidNetworkResponse<ReceivedShare> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        val probe = helper.probeReceivedAccess(webId, URI.create(resourceUri))
            ?: error("No access to $resourceUri")
        val ownerWebId = probe.second ?: deriveOwnerFallback(resourceUri)
        val share = ReceivedShare(
            ownerWebId = ownerWebId,
            mode = probe.first,
            resourceUri = resourceUri,
        )
        helper.replaceReceivedShare(webId, podRoot, share)
        share
    }

    override suspend fun removeReceivedShare(
        webId: String,
        resourceUri: String,
        ownerWebId: String,
    ): SolidNetworkResponse<Unit> = wrap {
        val podRoot = helper.getPodRoot(webId)
        helper.ensurePrivateSharesContainer(webId, podRoot)
        helper.removeReceivedShare(webId, podRoot, resourceUri, ownerWebId)
    }

    // ── Share URL helpers ───────────────────────────────────────────────────

    override fun getShareDeepLink(resourceUri: String): String =
        "$SOLID_SHARE_URI_SCHEME://share?u=" + Uri.encode(resourceUri)

    override fun parseShareDeepLink(deepLink: String): String? {
        if (!deepLink.startsWith("$SOLID_SHARE_URI_SCHEME://")) return null
        val parsed = runCatching { Uri.parse(deepLink) }.getOrNull() ?: return null
        return parsed.getQueryParameter("u")
    }

    // ── Internals ───────────────────────────────────────────────────────────

    private inline fun <T> wrap(block: () -> T): SolidNetworkResponse<T> = try {
        SolidNetworkResponse.Success(block())
    } catch (e: Exception) {
        SolidNetworkResponse.Exception(e)
    }

    private fun deriveOwnerFallback(resourceUri: String): String {
        // Best-effort: strip path → leaves just `https://pod.example.org`.
        // Better than nothing; refresh later may correct via solid:owner Link.
        return runCatching {
            val u = URI.create(resourceUri)
            "${u.scheme}://${u.authority}"
        }.getOrDefault(resourceUri)
    }

    private fun GivenShare.shareKey(): String =
        "${receiver.toRdfSubject()}|$resourceUri"

    private fun ReceivedShare.shareKey(): String =
        "$ownerWebId|$resourceUri"
}
