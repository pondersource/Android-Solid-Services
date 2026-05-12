// IASSGivenShareCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.sharing;

import com.erfangholami.androidsolidservices.shared.domain.sharing.GivenShare;

interface IASSGivenShareCallback {
    oneway void onResult(in @nullable GivenShare share);
    oneway void onError(int errorCode, String errorMessage);
}
