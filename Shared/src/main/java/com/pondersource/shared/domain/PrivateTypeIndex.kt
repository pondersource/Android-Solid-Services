package com.pondersource.shared.domain

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import okhttp3.Headers
import java.net.URI

class PrivateTypeIndex : SettingTypeIndex {

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) : super(identifier, mediaType, quads, headers)

    override fun setTypes() {
        val existing = quads.filter {
            it.subject == getIdentifier().toString() && it.predicate == typeKey
        }
        if (existing.isEmpty()) {
            addQuad(getIdentifier().toString(), typeKey, typeIndex, maxNumber = Int.MAX_VALUE)
            addQuad(getIdentifier().toString(), typeKey, unlistedDocument, maxNumber = Int.MAX_VALUE)
        }
    }
}
