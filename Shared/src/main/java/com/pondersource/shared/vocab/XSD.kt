package com.pondersource.shared.vocab

/**
 * XML Schema Datatypes (XSD) vocabulary constants.
 * http://www.w3.org/2001/XMLSchema#
 */
object XSD {
    const val NAMESPACE = "http://www.w3.org/2001/XMLSchema#"

    //String types
    const val STRING = "${NAMESPACE}string"
    const val NORMALIZED_STRING = "${NAMESPACE}normalizedString"
    const val TOKEN = "${NAMESPACE}token"
    const val LANGUAGE = "${NAMESPACE}language"
    const val ANY_URI = "${NAMESPACE}anyURI"
    const val LANG_STRING = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"

    //Numeric types
    const val INTEGER = "${NAMESPACE}integer"
    const val LONG = "${NAMESPACE}long"
    const val INT = "${NAMESPACE}int"
    const val SHORT = "${NAMESPACE}short"
    const val BYTE = "${NAMESPACE}byte"
    const val DECIMAL = "${NAMESPACE}decimal"
    const val FLOAT = "${NAMESPACE}float"
    const val DOUBLE = "${NAMESPACE}double"
    const val NON_NEGATIVE_INTEGER = "${NAMESPACE}nonNegativeInteger"
    const val POSITIVE_INTEGER = "${NAMESPACE}positiveInteger"
    const val NON_POSITIVE_INTEGER = "${NAMESPACE}nonPositiveInteger"
    const val NEGATIVE_INTEGER = "${NAMESPACE}negativeInteger"
    const val UNSIGNED_LONG = "${NAMESPACE}unsignedLong"
    const val UNSIGNED_INT = "${NAMESPACE}unsignedInt"

    //Boolean
    const val BOOLEAN = "${NAMESPACE}boolean"

    //Date/time types
    const val DATE_TIME = "${NAMESPACE}dateTime"
    const val DATE_TIME_STAMP = "${NAMESPACE}dateTimeStamp"
    const val DATE = "${NAMESPACE}date"
    const val TIME = "${NAMESPACE}time"
    const val DURATION = "${NAMESPACE}duration"
    const val YEAR_MONTH_DURATION = "${NAMESPACE}yearMonthDuration"
    const val DAY_TIME_DURATION = "${NAMESPACE}dayTimeDuration"
    const val G_YEAR = "${NAMESPACE}gYear"
    const val G_MONTH = "${NAMESPACE}gMonth"
    const val G_DAY = "${NAMESPACE}gDay"

    //Binary
    const val BASE64_BINARY = "${NAMESPACE}base64Binary"
    const val HEX_BINARY = "${NAMESPACE}hexBinary"
}
