package com.pondersource.shared.vocab

/**
 * RDF core vocabulary constants.
 * http://www.w3.org/1999/02/22-rdf-syntax-ns#
 */
public object RDF {
    public const val NAMESPACE: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"

    public const val TYPE: String = "${NAMESPACE}type"
    public const val PROPERTY: String = "${NAMESPACE}Property"
    public const val STATEMENT: String = "${NAMESPACE}Statement"
    public const val SUBJECT: String = "${NAMESPACE}subject"
    public const val PREDICATE: String = "${NAMESPACE}predicate"
    public const val OBJECT: String = "${NAMESPACE}object"
    public const val BAG: String = "${NAMESPACE}Bag"
    public const val SEQ: String = "${NAMESPACE}Seq"
    public const val ALT: String = "${NAMESPACE}Alt"
    public const val VALUE: String = "${NAMESPACE}value"
    public const val LANG_STRING: String = "${NAMESPACE}langString"
    public const val HTML: String = "${NAMESPACE}HTML"
    public const val XML_LITERAL: String = "${NAMESPACE}XMLLiteral"
    public const val NIL: String = "${NAMESPACE}nil"
    public const val FIRST: String = "${NAMESPACE}first"
    public const val REST: String = "${NAMESPACE}rest"
    public const val LIST: String = "${NAMESPACE}List"
}
