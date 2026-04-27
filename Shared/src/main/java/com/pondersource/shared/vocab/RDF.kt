 package com.pondersource.shared.vocab

/**
 * RDF core vocabulary constants.
 * http://www.w3.org/1999/02/22-rdf-syntax-ns#
 */
object RDF {
    const val NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"

    const val TYPE = "${NAMESPACE}type"
    const val PROPERTY = "${NAMESPACE}Property"
    const val STATEMENT = "${NAMESPACE}Statement"
    const val SUBJECT = "${NAMESPACE}subject"
    const val PREDICATE = "${NAMESPACE}predicate"
    const val OBJECT = "${NAMESPACE}object"
    const val BAG = "${NAMESPACE}Bag"
    const val SEQ = "${NAMESPACE}Seq"
    const val ALT = "${NAMESPACE}Alt"
    const val VALUE = "${NAMESPACE}value"
    const val LANG_STRING = "${NAMESPACE}langString"
    const val HTML = "${NAMESPACE}HTML"
    const val XML_LITERAL = "${NAMESPACE}XMLLiteral"
    const val NIL = "${NAMESPACE}nil"
    const val FIRST = "${NAMESPACE}first"
    const val REST = "${NAMESPACE}rest"
    const val LIST = "${NAMESPACE}List"
}
