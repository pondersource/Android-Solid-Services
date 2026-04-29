package com.pondersource.shared.domain

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.Solid
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI
import java.util.UUID

/**
 * Represents a Solid Type Index document (public or private).
 *
 * A type index maps RDF classes to specific resource instances or containers
 * in a pod. It contains `solid:TypeRegistration` entries, each with a
 * `solid:forClass` predicate and either `solid:instance` or
 * `solid:instanceContainer`.
 *
 * Spec: https://solid.github.io/webid-profile/ — Type Indexes
 */
abstract class SettingTypeIndex : SolidRDFResource {

    // String constants used by subclasses
    protected val typeKey = RDF.TYPE
    protected val typeRegistration = Solid.TYPE_REGISTRATION
    protected val forClassKey = Solid.FOR_CLASS
    protected val instanceKey = Solid.INSTANCE
    protected val instanceContainerKey = Solid.INSTANCE_CONTAINER
    protected val typeIndex = Solid.TYPE_INDEX
    protected val unlistedDocument = Solid.UNLISTED_DOCUMENT
    protected val listedDocument = Solid.LISTED_DOCUMENT
    protected val addressBook = VCARD.ADDRESS_BOOK

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) : super(identifier, mediaType, quads, headers)

    init {
        setTypes()
    }

    abstract fun setTypes()

    /**
     * Returns all `solid:instance` URIs registered for the given [forClass] IRI.
     */
    fun getInstances(forClass: String): List<String> =
        quads
            .filter { it.predicate == Solid.FOR_CLASS && it.`object` == forClass }
            .mapNotNull { registration ->
                quads.find { it.subject == registration.subject && it.predicate == Solid.INSTANCE }?.`object`
            }

    /**
     * Returns all `solid:instanceContainer` URIs registered for the given [forClass] IRI.
     */
    fun getInstanceContainers(forClass: String): List<String> =
        quads
            .filter { it.predicate == Solid.FOR_CLASS && it.`object` == forClass }
            .mapNotNull { registration ->
                quads.find { it.subject == registration.subject && it.predicate == Solid.INSTANCE_CONTAINER }?.`object`
            }

    /**
     * Registers a `solid:instance` entry for [forClass] pointing to [instanceUri].
     */
    fun addInstance(forClass: String, instanceUri: String) {
        val subject = "${getIdentifier()}#${UUID.randomUUID()}"
        addQuad(subject, RDF.TYPE, Solid.TYPE_REGISTRATION, maxNumber = Int.MAX_VALUE)
        addQuad(subject, Solid.FOR_CLASS, forClass, maxNumber = Int.MAX_VALUE)
        addQuad(subject, Solid.INSTANCE, instanceUri, maxNumber = Int.MAX_VALUE)
    }

    /**
     * Registers a `solid:instanceContainer` entry for [forClass] pointing to
     * [containerUri].
     */
    fun addInstanceContainer(forClass: String, containerUri: String) {
        val subject = "${getIdentifier()}#${UUID.randomUUID()}"
        addQuad(subject, RDF.TYPE, Solid.TYPE_REGISTRATION, maxNumber = Int.MAX_VALUE)
        addQuad(subject, Solid.FOR_CLASS, forClass, maxNumber = Int.MAX_VALUE)
        addQuad(subject, Solid.INSTANCE_CONTAINER, containerUri, maxNumber = Int.MAX_VALUE)
    }

    /**
     * Returns `true` if [resourceUri] is registered (as either instance or
     * instanceContainer) for any class.
     */
    fun containsResource(resourceUri: String): Boolean =
        quads.any { it.`object` == resourceUri }

    /**
     * Removes the registration entry that points to [resourceUri] (removes
     * the entire registration node including forClass and type triples).
     */
    fun removeResource(resourceUri: String) {
        val anchor = quads.find { it.`object` == resourceUri } ?: return
        quads.removeAll { it.subject == anchor.subject || it.`object` == anchor.subject }
    }

    fun getAddressBooks(): List<String> = getInstances(VCARD.ADDRESS_BOOK)

    fun addAddressBook(addressBook: String) = addInstance(VCARD.ADDRESS_BOOK, addressBook)

    fun containsAddressBook(addressBookUri: String): Boolean = containsResource(addressBookUri)

    fun removeAddressBook(addressBookUri: String) = removeResource(addressBookUri)
}
