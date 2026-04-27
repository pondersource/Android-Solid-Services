package com.pondersource.shared.vocab

/**
 * Shape Trees vocabulary constants.
 * http://www.w3.org/ns/shapetrees#
 * Spec: https://shapetrees.org/TR/specification/
 *
 * Shape Trees define the expected structure and shape-validation rules
 * for composite data in a Solid pod. They are used by SAI to describe
 * what data an application needs and how it is organized.
 */
object ShapeTree {
    const val NAMESPACE = "http://www.w3.org/ns/shapetrees#"

    //Types
    /**
     * Defines a composite data structure: a tree of resources and validation
     * criteria (linked shape, expected resource type, child shape trees).
     */
    const val SHAPE_TREE = "${NAMESPACE}ShapeTree"

    /** A shape tree that expects an LDP container resource. */
    const val CONTAINER = "${NAMESPACE}Container"

    /** A shape tree that expects an RDF resource (non-container). */
    const val RESOURCE = "${NAMESPACE}Resource"

    /** A shape tree that expects a binary (non-RDF) resource. */
    const val NON_RDF_RESOURCE = "${NAMESPACE}NonRDFResource"

    /** Describes how one shape tree references another. */
    const val REFERENCE = "${NAMESPACE}Reference"

    /**
     * Associates a managed resource with one or more shape tree assignments.
     * Stored in a shape tree manager auxiliary resource.
     */
    const val MANAGER = "${NAMESPACE}Manager"

    /**
     * Identifies a shape tree, the managed resource it is assigned to,
     * the focus node for validation, and the navigation path to reach it.
     */
    const val ASSIGNMENT = "${NAMESPACE}Assignment"

    /** An index of SKOS poly-hierarchies used to describe shape trees. */
    const val DESCRIPTION_SET = "${NAMESPACE}DescriptionSet"

    /** A human-readable description of a shape tree. */
    const val DESCRIPTION = "${NAMESPACE}Description"

    //Shape tree predicates
    /**
     * Describes the expected resource type for this shape tree node
     * (one of `st:Container`, `st:Resource`, `st:NonRDFResource`).
     */
    const val EXPECTS_TYPE = "${NAMESPACE}expectsType"

    /**
     * Links a shape tree to another shape tree directly contained within
     * its planted container hierarchy.
     */
    const val CONTAINS = "${NAMESPACE}contains"

    /** Links a shape tree to a Shape Tree Reference (for traversal via predicates). */
    const val REFERENCES = "${NAMESPACE}references"

    /** Links a shape tree to a Shape (ShEx or SHACL) for instance validation. */
    const val SHAPE = "${NAMESPACE}shape"

    /** Points to the shape tree being referenced in a Reference node. */
    const val REFERENCES_SHAPE_TREE = "${NAMESPACE}referencesShapeTree"

    /**
     * Describes traversal to a referenced shape tree via a ShapePath expression
     * through the RDF graph of an instance.
     */
    const val VIA_SHAPE_PATH = "${NAMESPACE}viaShapePath"

    /**
     * Describes traversal to a referenced shape tree by following a specific
     * RDF predicate in the instance data.
     */
    const val VIA_PREDICATE = "${NAMESPACE}viaPredicate"

    //Manager / assignment predicates
    /** Links a shape tree manager to a shape tree assignment. */
    const val HAS_ASSIGNMENT = "${NAMESPACE}hasAssignment"

    /** Links a shape tree manager to the managed resource. */
    const val MANAGES = "${NAMESPACE}manages"

    /** Links a managed resource back to the shape tree manager. */
    const val MANAGED_BY = "${NAMESPACE}managedBy"

    /** Identifies the root assignment within a planted shape tree hierarchy. */
    const val HAS_ROOT_ASSIGNMENT = "${NAMESPACE}hasRootAssignment"

    /**
     * Identifies the focus node (subject IRI) in the instance data
     * against which the linked shape is validated.
     */
    const val FOCUS_NODE = "${NAMESPACE}focusNode"

    /** The IRI of the shape tree assigned to the managed resource. */
    const val ASSIGNS = "${NAMESPACE}assigns"

    //Description predicates
    /** Links a description to the shape tree it describes. */
    const val DESCRIBES = "${NAMESPACE}describes"

    /** The description set this description belongs to. */
    const val IN_DESCRIPTION_SET = "${NAMESPACE}inDescriptionSet"

    /** The language used by the associated description resource. */
    const val USES_LANGUAGE = "${NAMESPACE}usesLanguage"
}
