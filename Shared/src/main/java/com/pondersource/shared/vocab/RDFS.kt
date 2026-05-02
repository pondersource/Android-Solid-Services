package com.pondersource.shared.vocab

public object RDFS {
    public const val NAMESPACE: String = "http://www.w3.org/2000/01/rdf-schema#"
    public const val CLASS: String = "${NAMESPACE}Class"
    public const val LABEL: String = "${NAMESPACE}label"
    public const val COMMENT: String = "${NAMESPACE}comment"
    public const val SEE_ALSO: String = "${NAMESPACE}seeAlso"
    public const val IS_DEFINED_BY: String = "${NAMESPACE}isDefinedBy"
    public const val SUB_CLASS_OF: String = "${NAMESPACE}subClassOf"
    public const val SUB_PROPERTY_OF: String = "${NAMESPACE}subPropertyOf"
    public const val DOMAIN: String = "${NAMESPACE}domain"
    public const val RANGE: String = "${NAMESPACE}range"
    public const val MEMBER: String = "${NAMESPACE}member"
    public const val LITERAL: String = "${NAMESPACE}Literal"
    public const val DATATYPE: String = "${NAMESPACE}Datatype"
    public const val CONTAINER: String = "${NAMESPACE}Container"
    public const val CONTAINER_MEMBERSHIP_PROPERTY: String = "${NAMESPACE}ContainerMembershipProperty"
}
