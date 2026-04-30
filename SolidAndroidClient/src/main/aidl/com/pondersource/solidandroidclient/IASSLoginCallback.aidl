// IASSLoginCallback.aidl
package com.pondersource.solidandroidclient;

oneway interface IASSLoginCallback {

    void onResult(boolean granted, String selectedWebId);
    void onError(int errorCode, String errorMessage);
}