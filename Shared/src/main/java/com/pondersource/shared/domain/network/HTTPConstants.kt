package com.pondersource.shared.domain.network

/**
 * HTTP header name constants.
 * See https://solidproject.org/TR/protocol for Solid-specific headers.
 */
object HTTPHeaderName {
    //Standard request headers
    const val AUTHORIZATION = "Authorization"
    const val ACCEPT = "Accept"
    const val CONTENT_TYPE = "Content-Type"
    const val CONTENT_LENGTH = "Content-Length"
    const val LINK = "Link"
    const val IF_NONE_MATCH = "If-None-Match"
    const val IF_MATCH = "If-Match"
    const val IF_UNMODIFIED_SINCE = "If-Unmodified-Since"
    const val IF_MODIFIED_SINCE = "If-Modified-Since"
    const val ORIGIN = "Origin"
    const val HOST = "Host"

    //Standard response headers
    const val ETAG = "ETag"
    const val LAST_MODIFIED = "Last-Modified"
    const val LOCATION = "Location"
    const val ALLOW = "Allow"
    const val VARY = "Vary"
    const val WWW_AUTHENTICATE = "WWW-Authenticate"

    //CORS headers
    const val ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin"
    const val ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods"
    const val ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers"
    const val ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers"
    const val ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age"

    //Content negotiation
    const val ACCEPT_PATCH = "Accept-Patch"
    const val ACCEPT_POST = "Accept-Post"
    const val ACCEPT_PUT = "Accept-Put"

    //DPoP (Solid-OIDC)
    const val DPOP = "DPoP"
    const val DPOP_NONCE = "DPoP-Nonce"

    //WAC (Web Access Control)
    /** WAC-Allow: user="read write", public="read" */
    const val WAC_ALLOW = "WAC-Allow"
}

/**
 * HTTP media type / content type constants.
 */
object HTTPAcceptType {
    //RDF formats
    const val JSON_LD = "application/ld+json"
    const val TURTLE = "text/turtle"
    const val N3 = "text/n3"
    const val N_TRIPLES = "application/n-triples"
    const val N_QUADS = "application/n-quads"
    const val TRIG = "application/trig"
    const val RDF_XML = "application/rdf+xml"
    const val JSON_RDF = "application/rdf+json"

    //General
    const val JSON = "application/json"
    const val OCTET_STREAM = "application/octet-stream"
    const val FORM_URL_ENCODED = "application/x-www-form-urlencoded"
    const val MULTIPART_FORM_DATA = "multipart/form-data"
    const val TEXT_PLAIN = "text/plain"
    const val TEXT_HTML = "text/html"
    const val ANY = "*/*"
}

/**
 * HTTP Link header relation type constants.
 * Used in Solid `Link:` headers.
 * See https://solidproject.org/TR/protocol
 */
object HTTPLinkRelation {
    /** The ACL resource associated with this resource (WAC and ACP). */
    const val ACL = "acl"

    /** A resource that describes this resource. */
    const val DESCRIBED_BY = "describedby"

    /** This resource describes the context resource (inverse of describedby). */
    const val DESCRIBES = "describes"

    /** The rdf:type(s) of this resource. */
    const val TYPE = "type"

    /**
     * The storage description resource.
     * Full IRI: http://www.w3.org/ns/solid/terms#storageDescription
     */
    const val STORAGE_DESCRIPTION = "http://www.w3.org/ns/solid/terms#storageDescription"

    /**
     * The owner of a storage.
     * Full IRI: http://www.w3.org/ns/solid/terms#owner
     */
    const val OWNER = "http://www.w3.org/ns/solid/terms#owner"

    /**
     * The OIDC issuer for this resource / WebID.
     * Full IRI: http://www.w3.org/ns/solid/terms#oidcIssuer
     */
    const val OIDC_ISSUER = "http://www.w3.org/ns/solid/terms#oidcIssuer"

    /** Canonical URL of a resource. */
    const val CANONICAL = "canonical"

    /** Predecessor version in a version chain. */
    const val PREDECESSOR_VERSION = "predecessor-version"

    /** Alternate representation. */
    const val ALTERNATE = "alternate"
}

/**
 * HTTP status code constants used in the Solid protocol.
 * See https://solidproject.org/TR/protocol
 */
object HTTPStatusCode {
    const val OK = 200
    const val CREATED = 201
    const val NO_CONTENT = 204

    const val MOVED_PERMANENTLY = 301
    const val TEMPORARY_REDIRECT = 307
    const val PERMANENT_REDIRECT = 308

    const val BAD_REQUEST = 400
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val METHOD_NOT_ALLOWED = 405
    const val CONFLICT = 409
    const val GONE = 410
    const val PRECONDITION_FAILED = 412
    const val UNPROCESSABLE_ENTITY = 422

    const val INTERNAL_SERVER_ERROR = 500
}
