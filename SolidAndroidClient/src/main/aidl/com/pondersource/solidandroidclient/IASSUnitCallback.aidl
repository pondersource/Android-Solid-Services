// IASSUnitCallback.aidl
package com.pondersource.solidandroidclient;

// Callback for AIDL operations that produce no result value on success
// (e.g. patch, deleteContainer).
interface IASSUnitCallback {
    void onResult();
    void onError(int errorCode, String errorMessage);
}
