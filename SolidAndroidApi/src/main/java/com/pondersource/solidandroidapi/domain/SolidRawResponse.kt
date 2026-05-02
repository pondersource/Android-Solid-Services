package com.pondersource.solidandroidapi.domain

import okhttp3.Headers
import java.net.URI

internal class SolidRawResponse(
    val statusCode: Int,
    val headers: Headers,
    val bodyBytes: ByteArray,
    val uri: URI,
) {
    val body: String by lazy { bodyBytes.toString(Charsets.UTF_8) }
    fun isSuccessful(): Boolean = statusCode in 200..299
}