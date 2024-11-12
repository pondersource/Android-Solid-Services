// IASSLoginCallback.aidl
package com.pondersource.solidandroidclient;

oneway interface IASSLoginCallback {

    void onResult(boolean granted);
    void onError(int errorCode, String errorMessage);
}