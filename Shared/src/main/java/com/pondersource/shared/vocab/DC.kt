package com.pondersource.shared.vocab

object DC {
    const val NAMESPACE = "http://purl.org/dc/terms/"
    const val ELEMENT_NAMESPACE = "http://purl.org/dc/elements/1.1/"

    // Dates
    const val CREATED = "${NAMESPACE}created"
    const val MODIFIED = "${NAMESPACE}modified"
    const val ISSUED = "${NAMESPACE}issued"
    const val DATE = "${NAMESPACE}date"

    // Descriptive
    const val TITLE = "${NAMESPACE}title"
    const val DESCRIPTION = "${NAMESPACE}description"
    const val SUBJECT = "${NAMESPACE}subject"
    const val LANGUAGE = "${NAMESPACE}language"
    const val RIGHTS = "${NAMESPACE}rights"
    const val SOURCE = "${NAMESPACE}source"
    const val FORMAT = "${NAMESPACE}format"

    // Identity
    const val IDENTIFIER = "${NAMESPACE}identifier"
    const val PUBLISHER = "${NAMESPACE}publisher"
    const val CREATOR = "${NAMESPACE}creator"
    const val CONTRIBUTOR = "${NAMESPACE}contributor"

    // Relations
    const val RELATION = "${NAMESPACE}relation"
    const val IS_PART_OF = "${NAMESPACE}isPartOf"
    const val HAS_PART = "${NAMESPACE}hasPart"
    const val REPLACES = "${NAMESPACE}replaces"
    const val REQUIRES = "${NAMESPACE}requires"
}
