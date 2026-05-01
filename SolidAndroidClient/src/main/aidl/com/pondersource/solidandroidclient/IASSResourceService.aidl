// IASSResourceService.aidl
package com.pondersource.solidandroidclient;

import com.pondersource.solidandroidclient.IASSSolidNonRdfResourceCallback;
import com.pondersource.solidandroidclient.IASSSolidRdfResourceCallback;
import com.pondersource.solidandroidclient.IASSSolidMetadataCallback;
import com.pondersource.solidandroidclient.IASSContainerCallback;
import com.pondersource.solidandroidclient.IASSUnitCallback;
import com.pondersource.shared.domain.resource.SolidNonRDFResource;
import com.pondersource.shared.domain.resource.SolidRDFResource;

interface IASSResourceService {
    void getWebId(String webId, IASSSolidRdfResourceCallback callback);

    void head(String webId, String resourceUrl, IASSSolidMetadataCallback callback);

    void create(String webId, in SolidNonRDFResource resource, IASSSolidNonRdfResourceCallback callback);
    void createRdf(String webId, in SolidRDFResource resource, IASSSolidRdfResourceCallback callback);

    void read(String webId, String resourceUrl, IASSSolidNonRdfResourceCallback callback);
    void readRdf(String webId, String resourceUrl, IASSSolidRdfResourceCallback callback);
    void readContainer(String webId, String containerUrl, IASSContainerCallback callback);

    // ifMatch: optional ETag for a conditional PUT (pass null to skip).
    void update(String webId, in SolidNonRDFResource resource, String ifMatch, IASSSolidNonRdfResourceCallback callback);
    void updateRdf(String webId, in SolidRDFResource resource, String ifMatch, IASSSolidRdfResourceCallback callback);

    // patchBody: serialised N3 Patch document (text/n3 content).
    void patch(String webId, String resourceUrl, String patchBody, IASSUnitCallback callback);

    void delete(String webId, in SolidNonRDFResource resource, IASSSolidNonRdfResourceCallback callback);
    void deleteRdf(String webId, in SolidRDFResource resource, IASSSolidRdfResourceCallback callback);

    // Recursively deletes a container and all of its contents.
    void deleteContainer(String webId, String containerUrl, IASSUnitCallback callback);
}
