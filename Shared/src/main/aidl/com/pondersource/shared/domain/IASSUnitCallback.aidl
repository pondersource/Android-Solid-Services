// IASSUnitCallback.aidl
package com.pondersource.shared.domain;

interface IASSUnitCallback {
    void onResult();
    void onError(int errorCode, String errorMessage);
}
