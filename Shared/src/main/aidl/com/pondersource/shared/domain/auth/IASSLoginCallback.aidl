// IASSLoginCallback.aidl
package com.pondersource.shared.domain.auth;

oneway interface IASSLoginCallback {
    void onResult(boolean granted, String selectedWebId);
    void onError(int errorCode, String errorMessage);
}