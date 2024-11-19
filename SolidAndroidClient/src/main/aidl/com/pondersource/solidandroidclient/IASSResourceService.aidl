// IASSResourceService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSNonRdfResourceCallback;
import com.pondersource.solidandroidclient.IASSRdfResourceCallback;
import com.pondersource.shared.NonRDFSource;
import com.pondersource.shared.RDFSource;

interface IASSResourceService {
    void getWebId(IASSRdfResourceCallback callback);
    void create(in NonRDFSource resource, IASSNonRdfResourceCallback callback);
    void createRdf(in RDFSource resource, IASSRdfResourceCallback callback);
    void read(String resourceUrl, IASSNonRdfResourceCallback callback);
    void readRdf(String resourceUrl, IASSRdfResourceCallback callback);
    void update(in NonRDFSource resource, IASSNonRdfResourceCallback callback);
    void updateRdf(in RDFSource resource, IASSRdfResourceCallback callback);
    void delete(in NonRDFSource resource, IASSNonRdfResourceCallback callback);
    void deleteRdf(in RDFSource resource, IASSRdfResourceCallback callback);
}