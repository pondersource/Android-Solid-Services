// IASSLoginCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.auth;

oneway interface IASSLoginCallback {
    void onResult(boolean granted, String selectedWebId);
    void onError(int errorCode, String errorMessage);
}