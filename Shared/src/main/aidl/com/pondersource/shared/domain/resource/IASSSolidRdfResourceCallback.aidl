// IASSSolidRdfResourceCallback.aidl
package com.pondersource.shared.domain.resource;

import com.pondersource.shared.domain.resource.SolidRDFResource;

interface IASSSolidRdfResourceCallback  {
    void onResult(in SolidRDFResource result);
    void onError(int errorCode, String errorMessage);
}