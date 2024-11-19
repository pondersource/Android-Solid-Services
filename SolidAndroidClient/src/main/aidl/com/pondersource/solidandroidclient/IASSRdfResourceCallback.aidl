// IASSRdfResourceCallback.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.shared.RDFSource;

interface IASSRdfResourceCallback  {
    void onResult(in RDFSource result);
    void onError(int errorCode, String errorMessage);
}