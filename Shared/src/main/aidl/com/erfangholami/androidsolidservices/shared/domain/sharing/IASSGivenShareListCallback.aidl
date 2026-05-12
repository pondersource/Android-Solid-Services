// IASSGivenShareListCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.sharing;

import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare;

interface IASSGivenShareListCallback {
    oneway void onResult(in List<GivenShare> shares);
    oneway void onError(int errorCode, String errorMessage);
}
