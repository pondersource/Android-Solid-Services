package com.pondersource.solidandroidapi

import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.JsonLdVersion
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.jsonld.uri.UriValidationPolicy
import com.pondersource.shared.domain.container.SolidContainer
import com.pondersource.shared.domain.network.HTTPAcceptType
import com.pondersource.shared.domain.network.HTTPHeaderName
import com.pondersource.shared.domain.resource.RDFResource
import okhttp3.Headers
import java.io.InputStream
import java.net.URI

/**
 * Parses HTTP responses from a Solid pod into strongly-typed resource objects.
 *
 * Resource type is determined by the target class, following the Solid Protocol's
 * content-type conventions:
 *  - LDP containers   → [SolidContainer]
 *  - RDF documents    → any [RDFResource] subclass (WebID, contact, type index, etc.)
 *  - Binary/other     → NonRDFSource
 *
 * All relative IRIs in the response body are resolved against the document's base URL
 * (the request URI), as required by the JSON-LD specification and the Solid Protocol.
 * This correctly handles servers that return root-relative IRIs (e.g. `/storage/thing.ttl`)
 * without any server-specific workarounds.
 *
 * Spec: https://solidproject.org/TR/protocol#reading-resources
 *       https://www.w3.org/TR/json-ld11/#base-iri
 */
internal object SolidResourceParser {

    fun <T> parse(response: SolidRawResponse, clazz: Class<T>): T {
        val contentType =
            response.headers[HTTPHeaderName.CONTENT_TYPE] ?: HTTPAcceptType.OCTET_STREAM
        return when {
            SolidContainer::class.java.isAssignableFrom(clazz) -> parseContainer(
                response,
                clazz,
                contentType
            )

            RDFResource::class.java.isAssignableFrom(clazz) -> parseRdf(
                response,
                clazz,
                contentType
            )

            else -> parseNonRdf(response, clazz, contentType)
        }
    }

    /**
     * Parses an LDP BasicContainer response.
     *
     * The base URI is set to the container's own URL so that any relative IRIs in the
     * `ldp:contains` listing are resolved correctly (e.g. `./child/` → absolute URL).
     */
    private fun <T> parseContainer(
        response: SolidRawResponse,
        clazz: Class<T>,
        contentType: String
    ): T {
        // JsonLdOptions.base(URI) is a builder method — call on the instance explicitly
        // to avoid Kotlin resolving the zero-arg `base()` getter as a property first.
        val options = JsonLdOptions()
        options.base = response.uri
        options.processingMode = JsonLdVersion.V1_1
        options.isProduceGeneralizedRdf = true
        options.uriValidation = UriValidationPolicy.SchemeOnly

        val quads =
            RDFResource.parseJsonLd(JsonDocument.of(response.bodyBytes.inputStream()), options)
        return clazz
            .getConstructor(
                URI::class.java,
                MediaType::class.java,
                List::class.java,
                Headers::class.java
            )
            .newInstance(response.uri, MediaType.of(contentType), quads, response.headers)
    }

    /**
     * Parses an RDF resource document (WebID profile, contact, type index, address book, etc.).
     *
     * The base URI anchors any relative subject/predicate/object IRIs the document may contain.
     * Response headers are passed through so that [SolidRDFResource] subclasses receive the
     * ETag, ACL URI, WAC-Allow, and allowed methods from the server response.
     */
    private fun <T> parseRdf(response: SolidRawResponse, clazz: Class<T>, contentType: String): T {
        val options = JsonLdOptions()
        options.base = response.uri
        options.rdfDirection = JsonLdOptions.RdfDirection.I18N_DATATYPE
        options.processingMode = JsonLdVersion.V1_1
        options.isProduceGeneralizedRdf = true
        options.uriValidation = UriValidationPolicy.SchemeOnly

        val quads =
            RDFResource.parseJsonLd(JsonDocument.of(response.bodyBytes.inputStream()), options)
        return clazz
            .getConstructor(
                URI::class.java,
                MediaType::class.java,
                List::class.java,
                Headers::class.java
            )
            .newInstance(response.uri, MediaType.of(contentType), quads, response.headers)
    }

    /**
     * Passes through a non-RDF response (images, PDFs, arbitrary binary files, etc.)
     * as a raw input stream with its content type preserved.
     */
    private fun <T> parseNonRdf(
        response: SolidRawResponse,
        clazz: Class<T>,
        contentType: String
    ): T {
        // Use raw bytes — the body has never been through a String conversion, so binary
        // content (JPEG, MP4, PDF, …) arrives intact.
        return clazz
            .getConstructor(URI::class.java, String::class.java, InputStream::class.java)
            .newInstance(response.uri, contentType, response.bodyBytes.inputStream())
    }
}
