// IASSRdfResourceCallback.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.shared.NonRDFSource;

interface IASSNonRdfResourceCallback  {
    void onResult(in NonRDFSource result);
    void onError(int errorCode, String errorMessage);
}