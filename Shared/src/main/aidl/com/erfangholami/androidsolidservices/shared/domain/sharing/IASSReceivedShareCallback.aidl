// IASSReceivedShareCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.sharing;

import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare;

interface IASSReceivedShareCallback {
    oneway void onResult(in @nullable ReceivedShare share);
    oneway void onError(int errorCode, String errorMessage);
}
