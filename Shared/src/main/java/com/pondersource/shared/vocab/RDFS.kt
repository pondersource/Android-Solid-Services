package com.pondersource.shared.vocab

object RDFS {
    const val NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#"
    const val CLASS = "${NAMESPACE}Class"
    const val LABEL = "${NAMESPACE}label"
    const val COMMENT = "${NAMESPACE}comment"
    const val SEE_ALSO = "${NAMESPACE}seeAlso"
    const val IS_DEFINED_BY = "${NAMESPACE}isDefinedBy"
    const val SUB_CLASS_OF = "${NAMESPACE}subClassOf"
    const val SUB_PROPERTY_OF = "${NAMESPACE}subPropertyOf"
    const val DOMAIN = "${NAMESPACE}domain"
    const val RANGE = "${NAMESPACE}range"
    const val MEMBER = "${NAMESPACE}member"
    const val LITERAL = "${NAMESPACE}Literal"
    const val DATATYPE = "${NAMESPACE}Datatype"
    const val CONTAINER = "${NAMESPACE}Container"
    const val CONTAINER_MEMBERSHIP_PROPERTY = "${NAMESPACE}ContainerMembershipProperty"
}
