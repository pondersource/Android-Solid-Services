package com.pondersource.shared.domain.util

import java.net.URI
import java.net.URL

fun encodeUri(uri: URI): URI {
    return try {
        URI(
            uri.scheme,
            uri.userInfo,
            uri.host,
            uri.port,
            uri.path,
            uri.query,
            uri.fragment,
        )
    } catch (_: Exception) {
        uri
    }
}

fun encodeUriString(raw: String): URI {
    return try {
        encodeUri(URI(raw))
    } catch (_: Exception) {
        try {
            val url = URL(raw)
            URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
        } catch (_: Exception) {
            URI.create(raw)
        }
    }
}