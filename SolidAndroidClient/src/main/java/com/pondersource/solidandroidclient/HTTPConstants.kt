package com.pondersource.solidandroidclient

object HTTPHeaderName {
    const val AUTHORIZATION = "Authorization"
    const val ACCEPT = "Accept"
    const val CONTENT_TYPE = "Content-Type"
}

object HTTPAcceptType {
    const val JSON_LD = "application/ld+json"
    const val OCTET_STREAM = "application/octet-stream"
    const val TRIG = "application/trig"
    const val TURTLE = "text/turtle"
    const val ANY = "*/*"
    const val JSON_RDF = "application/rdf+json"
    const val XML_RDF = "application/rdf+xml"
    const val N3_RDF = "text/rdf+xml"
    const val N_TRIPLES = "application/n-triples"
}