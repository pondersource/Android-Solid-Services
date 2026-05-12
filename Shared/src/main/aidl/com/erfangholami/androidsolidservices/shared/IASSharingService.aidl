// IASSharingService.aidl
package com.erfangholami.androidsolidservices.shared;

import com.erfangholami.androidsolidservices.shared.domain.IASSUnitCallback;
import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare;
import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare;
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSGivenShareCallback;
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSReceivedShareCallback;
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSGivenShareListCallback;
import com.erfangholami.androidsolidservices.shared.domain.sharing.IASSReceivedShareListCallback;

/**
 * IPC contract for the sharing feature. Mirrors
 * `com.erfangholami.androidsolidservices.api.sharing.SharingManager`.
 *
 * `mode` is the ordinal of `ShareMode`: 0 = READ, 1 = APPEND, 2 = WRITE.
 *
 * `receiverKind` distinguishes the three receiver flavors and `receiverValue`
 * carries the IRI when applicable:
 *   0 = WebID receiver, value = WebID URI
 *   1 = Group receiver,  value = vcard:Group URI
 *   2 = Public receiver, value ignored (may be null)
 */
interface IASSharingService {

    // ── Given shares ───────────────────────────────────────────────────────

    void getStoredGivenShares(String webId, IASSGivenShareListCallback callback);

    void refreshGivenShares(String webId, IASSGivenShareListCallback callback);

    void getGivenSharesForResource(
        String webId,
        String resourceUri,
        IASSGivenShareListCallback callback
    );

    void createShare(
        String webId,
        String resourceUri,
        int mode,
        int receiverKind,
        @nullable String receiverValue,
        IASSGivenShareCallback callback
    );

    void updateShare(
        String webId,
        String resourceUri,
        int mode,
        int receiverKind,
        @nullable String receiverValue,
        IASSGivenShareCallback callback
    );

    void revokeShare(
        String webId,
        String resourceUri,
        int receiverKind,
        @nullable String receiverValue,
        IASSUnitCallback callback
    );

    // ── Profile share ──────────────────────────────────────────────────────

    void createProfileShare(
        String webId,
        in List<String> selectedFieldPredicates,
        int mode,
        int receiverKind,
        @nullable String receiverValue,
        IASSGivenShareCallback callback
    );

    // ── Received shares ────────────────────────────────────────────────────

    void getStoredReceivedShares(String webId, IASSReceivedShareListCallback callback);

    void refreshReceivedShares(String webId, IASSReceivedShareListCallback callback);

    void addReceivedShare(
        String webId,
        String resourceUri,
        IASSReceivedShareCallback callback
    );

    void removeReceivedShare(
        String webId,
        String resourceUri,
        String ownerWebId,
        IASSUnitCallback callback
    );
}
