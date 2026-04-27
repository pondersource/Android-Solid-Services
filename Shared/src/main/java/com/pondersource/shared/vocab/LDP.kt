package com.pondersource.shared.vocab

/**
 * Linked Data Platform (LDP) vocabulary constants.
 * https://www.w3.org/ns/ldp#
 */
object LDP {
    const val NAMESPACE = "http://www.w3.org/ns/ldp#"

    //Resource Types
    const val RESOURCE = "${NAMESPACE}Resource"
    const val RDF_SOURCE = "${NAMESPACE}RDFSource"
    const val NON_RDF_SOURCE = "${NAMESPACE}NonRDFSource"
    const val CONTAINER = "${NAMESPACE}Container"
    const val BASIC_CONTAINER = "${NAMESPACE}BasicContainer"
    const val DIRECT_CONTAINER = "${NAMESPACE}DirectContainer"
    const val INDIRECT_CONTAINER = "${NAMESPACE}IndirectContainer"

    //Container Predicates
    /** Links a container to the resources it contains. */
    const val CONTAINS = "${NAMESPACE}contains"

    /** Container membership predicate (DirectContainer / IndirectContainer). */
    const val MEMBERSHIP_RESOURCE = "${NAMESPACE}membershipResource"
    const val HAS_MEMBER_RELATION = "${NAMESPACE}hasMemberRelation"
    const val IS_MEMBER_OF_RELATION = "${NAMESPACE}isMemberOfRelation"
    const val INSERTED_CONTENT_RELATION = "${NAMESPACE}insertedContentRelation"
    const val MEMBER = "${NAMESPACE}member"

    //Paging
    const val PAGE_SEQUENCE = "${NAMESPACE}pageSequence"
    const val NEXT_PAGE = "${NAMESPACE}nextPage"
    const val PAGE = "${NAMESPACE}Page"

    //Other predicates
    const val INBOX = "${NAMESPACE}inbox"
    const val CONSTRAINED_BY = "${NAMESPACE}constrainedBy"
}
