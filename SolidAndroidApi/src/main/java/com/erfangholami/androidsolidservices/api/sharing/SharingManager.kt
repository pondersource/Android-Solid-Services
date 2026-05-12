package com.erfangholami.androidsolidservices.api.sharing

import com.erfangholami.androidsolidservices.shared.domain.network.SolidNetworkResponse
import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.ProfileShareConfig
import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareMode
import com.erfangholami.androidsolidservices.shared.domain.sharing.ShareReceiver
import com.erfangholami.androidsolidservices.api.auth.Authenticator
import com.erfangholami.androidsolidservices.api.resource.SolidResourceManager
import com.erfangholami.androidsolidservices.api.sharing.implementation.SharingManagerImplementation

/**
 * Creates, lists, and revokes shares of pod resources and profile snapshots.
 *
 * Sharing is built entirely on Web Access Control (WAC). The manager keeps a
 * private bookkeeping pair (`given_shares.ttl`, `received_shares.ttl`) under
 * `{podRoot}/.shares/` so the user can see what they have shared and what
 * they have received without re-walking the pod every time.
 *
 * For profile shares, a snapshot RDF document and a public HTML business card
 * are written under `{podRoot}/solidshare/profiles/`; the snapshot URI is the
 * share URL.
 */
public interface SharingManager {

    public companion object {
        public fun getInstance(authenticator: Authenticator): SharingManager =
            SharingManagerImplementation.getInstance(authenticator)

        public fun getInstance(resourceManager: SolidResourceManager): SharingManager =
            SharingManagerImplementation.getInstance(resourceManager)
    }

    // ── Given shares ────────────────────────────────────────────────────────

    /**
     * Returns the locally-tracked given shares (fast — single HTTP read of
     * the index file). Use [refreshGivenShares] to re-validate against the
     * actual ACLs on the pod.
     */
    public suspend fun getStoredGivenShares(
        webId: String,
    ): SolidNetworkResponse<List<GivenShare>>

    /**
     * Re-validates each tracked given share by re-reading the relevant
     * resource ACL. Drops any entries whose ACL no longer grants the receiver
     * the recorded mode and persists the verified list back to the index.
     */
    public suspend fun refreshGivenShares(
        webId: String,
    ): SolidNetworkResponse<List<GivenShare>>

    /**
     * Returns only the given shares affecting [resourceUri] — read directly
     * from that resource's ACL (authoritative, may differ from the index).
     */
    public suspend fun getGivenSharesForResource(
        webId: String,
        resourceUri: String,
    ): SolidNetworkResponse<List<GivenShare>>

    /**
     * Adds (or replaces) a WAC authorization on [resourceUri] granting [mode]
     * to [receiver], and updates the given-shares index.
     *
     * If a share for the same `(resource, receiver)` pair exists, it is replaced
     * with the new mode.
     */
    public suspend fun createShare(
        webId: String,
        resourceUri: String,
        mode: ShareMode,
        receiver: ShareReceiver,
    ): SolidNetworkResponse<GivenShare>

    /**
     * Updates the access mode of an existing share. Equivalent to [createShare]
     * with a different mode.
     */
    public suspend fun updateShare(
        webId: String,
        resourceUri: String,
        mode: ShareMode,
        receiver: ShareReceiver,
    ): SolidNetworkResponse<GivenShare>

    /**
     * Removes the WAC authorization for [receiver] on [resourceUri] and removes
     * the matching index triple.
     */
    public suspend fun revokeShare(
        webId: String,
        resourceUri: String,
        receiver: ShareReceiver,
    ): SolidNetworkResponse<Unit>

    // ── Profile share ───────────────────────────────────────────────────────

    /**
     * Creates a profile snapshot and a public HTML business card from the
     * fields selected in [config], grants the receiver the given mode on the
     * snapshot, and adds the snapshot URI to the given-shares index.
     *
     * The snapshot URI is the share URL (encode in QR / send to receiver).
     */
    public suspend fun createProfileShare(
        webId: String,
        config: ProfileShareConfig,
        mode: ShareMode = ShareMode.READ,
    ): SolidNetworkResponse<GivenShare>

    // ── Received shares ─────────────────────────────────────────────────────

    /**
     * Returns the locally-tracked received shares (fast). Use
     * [refreshReceivedShares] to re-validate.
     */
    public suspend fun getStoredReceivedShares(
        webId: String,
    ): SolidNetworkResponse<List<ReceivedShare>>

    /**
     * Re-validates each tracked received share via HEAD (uses `WAC-Allow` and
     * `Link rel="solid:owner"`). Stale entries are dropped.
     */
    public suspend fun refreshReceivedShares(
        webId: String,
    ): SolidNetworkResponse<List<ReceivedShare>>

    /**
     * Adds [resourceUri] to the received-shares index after verifying access.
     * Called when the user scans a QR code or pastes a share URL.
     */
    public suspend fun addReceivedShare(
        webId: String,
        resourceUri: String,
    ): SolidNetworkResponse<ReceivedShare>

    /**
     * Removes a tracked received share. Does not affect the resource itself.
     */
    public suspend fun removeReceivedShare(
        webId: String,
        resourceUri: String,
        ownerWebId: String,
    ): SolidNetworkResponse<Unit>

    // ── Share URL / QR helpers ──────────────────────────────────────────────

    /**
     * Encoding used inside QR codes. Returns a `solidshare://` deep-link wrapping
     * the original [resourceUri] so the SolidShare app picks it up directly,
     * with the original https URL as a query parameter for non-Solid scanners.
     */
    public fun getShareDeepLink(resourceUri: String): String

    /** Extracts the original resource URI from a [getShareDeepLink] string. */
    public fun parseShareDeepLink(deepLink: String): String?
}
