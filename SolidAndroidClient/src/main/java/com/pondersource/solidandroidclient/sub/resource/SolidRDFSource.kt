package com.pondersource.solidandroidclient.sub.resource

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import okhttp3.Headers
import java.net.URI

open class SolidRDFSource: RDFSource, SolidResources {

    //TODO("Add metadata")

    constructor(
        identifier: URI
    ): this (identifier, null)

    constructor(
        identifier: URI,
        dataset: RdfDataset?
    ) : this(identifier, dataset, null)

    constructor(
        identifier: URI,
        dataset: RdfDataset?,
        headers: Headers?
    ) : this(identifier, MediaType.JSON_LD,dataset, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ) : super(identifier, mediaType, dataset, headers)
}