// IASSResourceService.aidl
package com.erfangholami.androidsolidservices.shared;

import com.erfangholami.androidsolidservices.shared.domain.resource.IASSSolidNonRdfResourceCallback;
import com.erfangholami.androidsolidservices.shared.domain.resource.IASSSolidRdfResourceCallback;
import com.erfangholami.androidsolidservices.shared.domain.resource.IASSSolidMetadataCallback;
import com.erfangholami.androidsolidservices.shared.domain.resource.IASSContainerCallback;
import com.erfangholami.androidsolidservices.shared.domain.IASSUnitCallback;
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidNonRDFResource;
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource;

interface IASSResourceService {
    void getWebId(String webId, IASSSolidRdfResourceCallback callback);

    void head(String webId, String resourceUrl, IASSSolidMetadataCallback callback);

    void create(String webId, in SolidNonRDFResource resource, IASSSolidNonRdfResourceCallback callback);
    void createRdf(String webId, in SolidRDFResource resource, IASSSolidRdfResourceCallback callback);

    void read(String webId, String resourceUrl, IASSSolidNonRdfResourceCallback callback);
    void readRdf(String webId, String resourceUrl, IASSSolidRdfResourceCallback callback);
    void readContainer(String webId, String containerUrl, IASSContainerCallback callback);

    void update(String webId, in SolidNonRDFResource resource, String ifMatch, IASSSolidNonRdfResourceCallback callback);
    void updateRdf(String webId, in SolidRDFResource resource, String ifMatch, IASSSolidRdfResourceCallback callback);
    void patch(String webId, String resourceUrl, String patchBody, IASSUnitCallback callback);

    void delete(String webId, in SolidNonRDFResource resource, IASSSolidNonRdfResourceCallback callback);
    void deleteRdf(String webId, in SolidRDFResource resource, IASSSolidRdfResourceCallback callback);
    void deleteContainer(String webId, String containerUrl, IASSUnitCallback callback);
}
