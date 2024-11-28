package com.pondersource.shared.data

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.Solid
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI
import java.util.UUID

abstract class SettingTypeIndex: RDFSource {

    protected val typeKey = rdf.createIRI(RDF.type.toString())
    protected val typeRegistration = rdf.createIRI(Solid.typeRegistration)
    protected val forClass = rdf.createIRI(Solid.forClass)
    protected val instance = rdf.createIRI(Solid.instance)
    protected val AddressBook = rdf.createIRI(VCARD.AddressBook)
    protected val TypeIndex = rdf.createIRI(Solid.TypeIndex)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ): super(identifier, mediaType, dataset, headers)

    init {
        setTypes()
    }

    abstract fun setTypes()

    fun getDataSet() = dataset

    fun getAddressBooks(): List<String> {
        val list = dataset.defaultGraph.toList()
        return list.filter { it.predicate.equals(forClass) && it.`object`.equals(AddressBook) }.map { triple ->
            list.find { it.predicate.equals(instance) && it.subject.equals(triple.subject) }!!.`object`.value
        }
    }

    fun addAddressBook(addressBook: String) {
        val id = UUID.randomUUID().toString()
        val subject = rdf.createIRI("${getIdentifier()}#${id}")
        addTriple(
            rdf.createTriple(
                subject,
                typeKey,
                typeRegistration
            ),
        )
        addTriple(
            rdf.createTriple(
                subject,
                forClass,
                AddressBook
            )
        )
        addTriple(
            rdf.createTriple(
                subject,
                instance,
                rdf.createIRI(addressBook)
            )
        )
    }
}