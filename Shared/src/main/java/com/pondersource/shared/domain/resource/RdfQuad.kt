package com.pondersource.shared.domain.resource

/**
 * Lightweight representation of an RDF quad (triple + optional named graph).
 *
 * Object interpretation:
 * - `datatype == null && language == null` → IRI or blank-node object
 * - `datatype != null` → typed literal (e.g. `xsd:string`, `xsd:dateTime`)
 * - `language != null` → language-tagged string literal
 *
 * Blank nodes use the `"_:"` prefix per the N-Quads / RdfQuadConsumer convention.
 */
data class RdfQuad(
    val subject: String,
    val predicate: String,
    val `object`: String,
    val datatype: String? = null,
    val language: String? = null,
    val graph: String? = null
) {
    val isLiteralObject: Boolean get() = datatype != null || language != null

    val isBlankSubject: Boolean get() = subject.startsWith("_:")

    val isBlankObject: Boolean get() = !isLiteralObject && `object`.startsWith("_:")
}