// IASSContainerCallback.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.shared.domain.container.SolidContainer;

interface IASSContainerCallback {
    void onResult(in SolidContainer result);
    void onError(int errorCode, String errorMessage);
}
