package com.pondersource.shared.domain.util

import java.net.URI
import java.net.URISyntaxException

/**
 * Utilities for working with IRIs (Internationalized Resource Identifiers)
 * in the context of the Solid protocol.
 *
 * Java's [URI] class only handles a subset of valid IRIs (ASCII-range). These
 * helpers provide basic normalization and validation aligned with the Solid
 * Protocol's IRI requirements.
 *
 * Spec: https://solidproject.org/TR/protocol — IRI handling
 */
object IriUtils {

    private val VALID_SCHEMES = setOf("http", "https")

    /**
     * Returns `true` if [iri] is a syntactically valid absolute IRI with an
     * `http` or `https` scheme (the only schemes used in Solid).
     */
    fun isValid(iri: String): Boolean {
        if (iri.isBlank()) return false
        return try {
            val uri = URI(iri)
            uri.isAbsolute && uri.scheme in VALID_SCHEMES
        } catch (_: URISyntaxException) {
            false
        }
    }

    /**
     * Returns `true` if [iri] identifies an LDP container (URI ends with `/`).
     * Per the Solid Protocol, the `/` character indicates a hierarchical
     * container relationship.
     */
    fun isContainer(iri: String): Boolean = iri.endsWith("/")

    /**
     * Appends a trailing slash to [iri] if absent, returning a container IRI.
     */
    fun toContainerIri(iri: String): String =
        if (iri.endsWith("/")) iri else "$iri/"

    /**
     * Strips the trailing slash from [iri] if present.
     */
    fun stripTrailingSlash(iri: String): String =
        if (iri.endsWith("/")) iri.dropLast(1) else iri

    /**
     * Returns the parent container IRI of [iri], or `null` if [iri] is the
     * storage root (no parent).
     *
     * Examples:
     * - `https://pod.example/foo/bar/baz`  → `https://pod.example/foo/bar/`
     * - `https://pod.example/foo/bar/`     → `https://pod.example/foo/`
     * - `https://pod.example/`             → `null`
     */
    fun parentContainer(iri: String): String? {
        val normalized = stripTrailingSlash(iri)
        val lastSlash  = normalized.lastIndexOf('/')
        if (lastSlash <= normalized.indexOf("//") + 1) return null
        return normalized.substring(0, lastSlash + 1)
    }

    /**
     * Returns the last path segment of [iri] (the resource name),
     * excluding any trailing slash.
     *
     * Examples:
     * - `https://pod.example/foo/bar.ttl`  → `bar.ttl`
     * - `https://pod.example/foo/bar/`     → `bar`
     */
    fun resourceName(iri: String): String {
        val normalized = stripTrailingSlash(iri)
        return normalized.substringAfterLast('/')
    }

    /**
     * Attempts to parse [iri] as a [URI], returning `null` on failure instead
     * of throwing.
     */
    fun toUriOrNull(iri: String): URI? =
        runCatching { URI.create(iri) }.getOrNull()

    /**
     * Resolves [reference] against [base], returning the resolved IRI string.
     * Handles relative references such as `/path/to/resource`.
     */
    fun resolve(base: String, reference: String): String =
        URI.create(base).resolve(reference).toString()
}
