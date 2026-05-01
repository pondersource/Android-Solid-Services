// IASSSolidMetadataCallback.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.shared.domain.resource.SolidMetadata;

interface IASSSolidMetadataCallback {
    void onResult(in SolidMetadata result);
    void onError(int errorCode, String errorMessage);
}
