// IASSContainerCallback.aidl
package com.pondersource.shared.domain.resource;

import com.pondersource.shared.domain.resource.SolidContainer;

interface IASSContainerCallback {
    void onResult(in SolidContainer result);
    void onError(int errorCode, String errorMessage);
}
