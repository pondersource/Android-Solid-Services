// IASSResourceService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSSolidNonRdfResourceCallback;
import com.pondersource.solidandroidclient.IASSSolidRdfResourceCallback;
import com.pondersource.solidandroidclient.IASSUnitCallback;
import com.pondersource.shared.domain.resource.SolidNonRDFResource;
import com.pondersource.shared.domain.resource.SolidRDFResource;

interface IASSResourceService {
    void getWebId(IASSSolidRdfResourceCallback callback);

    void create(in SolidNonRDFResource resource, IASSSolidNonRdfResourceCallback callback);
    void createRdf(in SolidRDFResource resource, IASSSolidRdfResourceCallback callback);

    void read(String resourceUrl, IASSSolidNonRdfResourceCallback callback);
    void readRdf(String resourceUrl, IASSSolidRdfResourceCallback callback);

    // ifMatch: optional ETag for a conditional PUT (pass null to skip).
    void update(in SolidNonRDFResource resource, String ifMatch, IASSSolidNonRdfResourceCallback callback);
    void updateRdf(in SolidRDFResource resource, String ifMatch, IASSSolidRdfResourceCallback callback);

    // patchBody: serialised N3 Patch document (text/n3 content).
    void patch(String resourceUrl, String patchBody, IASSUnitCallback callback);

    void delete(in SolidNonRDFResource resource, IASSSolidNonRdfResourceCallback callback);
    void deleteRdf(in SolidRDFResource resource, IASSSolidRdfResourceCallback callback);

    // Recursively deletes a container and all of its contents.
    void deleteContainer(String containerUrl, IASSUnitCallback callback);
}
