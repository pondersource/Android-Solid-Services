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

class SettingTypeIndex: RDFSource {

    private val typeKey = rdf.createIRI(RDF.type.toString())
    private val typeRegistration = rdf.createIRI(Solid.typeRegistration)
    private val forClass = rdf.createIRI(Solid.forClass)
    private val instance = rdf.createIRI(Solid.instance)
    private val AddressBook = rdf.createIRI(VCARD.AddressBook)
    private val TypeIndex = rdf.createIRI(Solid.TypeIndex)
    private val UnlistedDocument = rdf.createIRI(Solid.UnlistedDocument)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ): super(identifier, mediaType, dataset, headers)

    init {
        setTypes()
    }

    private fun setTypes() {
        val types = dataset.defaultGraph.toList().filter {
            it.subject.value == getIdentifier().toString() && it.predicate.equals(typeKey)
        }

        if(types.isEmpty()) {
            addTriple(
                rdf.createTriple(
                    rdf.createIRI(getIdentifier().toString()),
                    typeKey,
                    TypeIndex
                ),
                Int.MAX_VALUE
            )
            addTriple(
                rdf.createTriple(
                    rdf.createIRI(getIdentifier().toString()),
                    typeKey,
                    UnlistedDocument
                ),
                Int.MAX_VALUE
            )
        }
    }

    fun getAddressBooks(): List<URI> {
        val list = dataset.defaultGraph.toList()
        return list.filter { it.predicate.equals(forClass) && it.`object`.equals(AddressBook) }.map { triple ->
            list.find { it.predicate.equals(instance) && it.subject.equals(triple.subject) }!!.`object`.value
        }.map {
            URI.create(it)
        }
    }

    fun addAddressBook(addressBook: String) {
        val id = UUID.randomUUID().toString()
        val subject = rdf.createIRI("${getIdentifier().toString()}#${id}")
        addTriple(
            rdf.createTriple(
                subject,
                typeKey,
                typeRegistration
            )
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

    fun removeExtra(){
        val subject = rdf.createIRI("https://storage.inrupt.com/4a1ea008-8f12-4451-8cce-1f6f0be6b2ce/settings/privateTypeIndexfc37af7c-561c-42f6-8d59-a7c37e8094fe")
        val list = dataset.defaultGraph.toList()
        dataset = rdf.createDataset().apply {
            list.filter { !it.subject.equals(subject) }.forEach {
                add(it)
            }
        }
    }
}