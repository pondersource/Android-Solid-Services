// IASSSolidNonRdfResourceCallback.aidl
package com.pondersource.shared.domain.resource;

import com.pondersource.shared.domain.resource.SolidNonRDFResource;

interface IASSSolidNonRdfResourceCallback  {
    void onResult(in SolidNonRDFResource result);
    void onError(int errorCode, String errorMessage);
}