// IASSContainerCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.resource;

import com.erfangholami.androidsolidservices.shared.domain.resource.SolidContainer;

interface IASSContainerCallback {
    void onResult(in SolidContainer result);
    void onError(int errorCode, String errorMessage);
}
