// IASSSolidRdfResourceCallback.aidl
package com.erfangholami.androidsolidservices.shared.domain.resource;

import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource;

interface IASSSolidRdfResourceCallback  {
    void onResult(in SolidRDFResource result);
    void onError(int errorCode, String errorMessage);
}