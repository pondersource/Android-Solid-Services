// IASSSolidMetadataCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.resource;

import com.erfangholami.androidsolidservices.shared.domain.resource.SolidMetadata;

interface IASSSolidMetadataCallback {
    void onResult(in SolidMetadata result);
    void onError(int errorCode, String errorMessage);
}
