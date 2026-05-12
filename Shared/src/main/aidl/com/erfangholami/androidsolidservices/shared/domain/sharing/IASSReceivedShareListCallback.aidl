// IASSReceivedShareListCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.sharing;

import com.erfangholami.androidsolidservices.shared.domain.sharing.ReceivedShare;

interface IASSReceivedShareListCallback {
    oneway void onResult(in List<ReceivedShare> shares);
    oneway void onError(int errorCode, String errorMessage);
}
