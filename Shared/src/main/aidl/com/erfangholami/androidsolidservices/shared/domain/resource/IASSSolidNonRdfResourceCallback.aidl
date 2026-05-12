// IASSSolidNonRdfResourceCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.resource;

import com.erfangholami.androidsolidservices.shared.domain.resource.SolidNonRDFResource;

interface IASSSolidNonRdfResourceCallback  {
    void onResult(in SolidNonRDFResource result);
    void onError(int errorCode, String errorMessage);
}