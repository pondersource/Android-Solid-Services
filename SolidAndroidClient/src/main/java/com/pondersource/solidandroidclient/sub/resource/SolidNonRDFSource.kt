package com.pondersource.solidandroidclient.sub.resource

import com.pondersource.solidandroidclient.NonRDFSource
import okhttp3.Headers
import java.io.InputStream
import java.net.URI

class SolidNonRDFSource: NonRDFSource, SolidResources {

    //TODO("Add metadata")
    constructor(
        identifier: URI,
        contentType: String,
        entity: InputStream
    ) : this(identifier, contentType, entity, null)

    constructor(
        identifier: URI,
        contentType: String,
        entity: InputStream,
        headers: Headers?
    ): super(identifier, contentType, headers, entity)


}