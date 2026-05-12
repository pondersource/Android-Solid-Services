// IASSUnitCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain;

interface IASSUnitCallback {
    void onResult();
    void onError(int errorCode, String errorMessage);
}
