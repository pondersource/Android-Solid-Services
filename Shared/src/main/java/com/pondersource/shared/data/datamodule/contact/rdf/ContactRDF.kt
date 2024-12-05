package com.pondersource.shared.data.datamodule.contact.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.data.datamodule.contact.Email
import com.pondersource.shared.data.datamodule.contact.Name
import com.pondersource.shared.data.datamodule.contact.PhoneNumber
import com.pondersource.shared.data.datamodule.contact.URLType
import com.pondersource.shared.vocab.ExtendedXsdConstants
import com.pondersource.shared.vocab.OWL
import com.pondersource.shared.vocab.VCARD
import okhttp3.Headers
import java.net.URI

class ContactRDF: RDFSource {

    private val rdfKey = rdf.createIRI(RDF.type.toString())
    private val rdfValue = rdf.createIRI(VCARD.Individual)
    private val fullNameKey = rdf.createIRI(VCARD.fn)
    private val hasUIDKey = rdf.createIRI(VCARD.hasUID)
    private val hasNameKey = rdf.createIRI(VCARD.hasName)
    private val hasPhotoKey = rdf.createIRI(VCARD.hasPhoto)
    private val hasRelatedKey = rdf.createIRI(VCARD.hasRelated)
    private val urlKey = rdf.createIRI(VCARD.url)
    private val hasAddressKey = rdf.createIRI(VCARD.hasAddress)
    private val bdayKey = rdf.createIRI(VCARD.bday)
    private val anniversaryKey = rdf.createIRI(VCARD.anniverary)
    private val hasEmailKey = rdf.createIRI(VCARD.hasEmail)
    private val valueKey = rdf.createIRI(VCARD.value)
    private val hasTelephoneKey = rdf.createIRI(VCARD.hasTelephone)
    private val organizationNameKey = rdf.createIRI(VCARD.organizationName)
    private val roleKey = rdf.createIRI(VCARD.role)
    private val titleKey = rdf.createIRI(VCARD.title)
    private val noteKey = rdf.createIRI(VCARD.note)
    private val sameAsKey = rdf.createIRI(OWL.sameAs)
    private val inAddressBookKey = rdf.createIRI(VCARD.inAddressBook)
    private val familyNameKey = rdf.createIRI(VCARD.familyName)
    private val givenNameKey = rdf.createIRI(VCARD.givenName)
    private val additionalNameKey = rdf.createIRI(VCARD.additionalName)
    private val honorificPrefixKey = rdf.createIRI(VCARD.honorificPrefix)
    private val honorificSuffixKey = rdf.createIRI(VCARD.honorificSuffix)

    constructor(
        identifier: URI,
        mediaType: MediaType? = null,
        dataset: RdfDataset? = null,
        headers: Headers? = null
    ): super(identifier, mediaType ?: MediaType.JSON_LD, dataset, headers)

    init {
        setType()
    }

    private fun setType() {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                rdfKey,
                rdfValue
            )
        )
    }

    fun getFullName(): String {
        return dataset.defaultGraph.toList()
            .find { it.subject.value == getIdentifier().toString() && it.predicate.equals(fullNameKey) }!!
            .`object`.value
    }

    fun setFullName(name: String) {
        addTriple(
            rdf.createTriple(
                rdf.createIRI(getIdentifier().toString()),
                fullNameKey,
                rdf.createTypedString(name, ExtendedXsdConstants.string)
            )
        )
    }

    fun getPhotoUrl(): String? {
        return dataset.defaultGraph.toList()
            .find { it.subject.value == getIdentifier().toString() && it.predicate.equals(hasPhotoKey) }?.`object`?.value
    }

    fun getUrls(): List<Pair<URLType, String>> {
        val returnList = arrayListOf<Pair<URLType, String>>()
        val list = dataset.defaultGraph.toList()
        list.filter {
            it.subject.value == getIdentifier().toString() && it.predicate.equals(urlKey)
        }.forEach { triple ->
            val vars = list.filter { it.subject.value == triple.`object`.value }
            val type : URLType = when(vars.find { it.predicate.equals(rdfKey) }?.`object`?.value) {
                VCARD.Home -> URLType.Home
                VCARD.Work -> URLType.Work
                VCARD.Homepage -> URLType.Homepage
                VCARD.WebId -> URLType.WebId
                VCARD.PublicId -> URLType.PublicId
                else -> URLType.Home
            }
            val url = vars.find { it.predicate.equals(VCARD.value) }!!.`object`.value
            returnList.add(Pair(type, url))
        }
        return returnList
    }

    fun getName(): Name? {
        val list = dataset.defaultGraph.toList()
        val hasName = list.find { it.subject.value == getIdentifier().toString() && it.predicate.equals(hasNameKey) }
        if (hasName != null) {
            return Name(
                list.find { it.subject.equals(hasName.`object`) && it.predicate.equals(familyNameKey) }?.`object`?.value,
                list.find { it.subject.equals(hasName.`object`) && it.predicate.equals(givenNameKey) }?.`object`?.value,
                list.find {
                    it.subject.equals(hasName.`object`) && it.predicate.equals(
                        additionalNameKey
                    )
                }?.`object`?.value,
                list.find {
                    it.subject.equals(hasName.`object`) && it.predicate.equals(
                        honorificPrefixKey
                    )
                }?.`object`?.value,
                list.find {
                    it.subject.equals(hasName.`object`) && it.predicate.equals(
                        honorificSuffixKey
                    )
                }?.`object`?.value,
            )
        } else {
            return null
        }
    }

    fun getPhoneNumbers(): List<PhoneNumber> {
        val returnList = arrayListOf<PhoneNumber>()
        val list = dataset.defaultGraph.toList()
        list.filter { it.subject.value == getIdentifier().toString() && it.predicate.equals(hasTelephoneKey) }
            .forEach { triple ->
                returnList.add(
                    PhoneNumber(
                        list.find { it.subject.equals(triple.`object`) && it.predicate.equals(valueKey) }!!.`object`.value
                    )
                )
            }

        return returnList
    }

    fun addPhoneNumber(newPhoneNumber: String?): Boolean {
        if(!newPhoneNumber.isNullOrEmpty()) {
            val alreadyExistPhoneNumber = dataset.defaultGraph.toList()
                .find { it.predicate.equals(valueKey) && it.`object`.value == "tel:${newPhoneNumber}" }

            if (alreadyExistPhoneNumber != null) {
                return false
            } else {
                val namedNode = rdf.createBlankNode(newPhoneNumber)
                addTriple(
                    rdf.createTriple(
                        rdf.createIRI(getIdentifier().toString()),
                        hasTelephoneKey,
                        namedNode
                    ),
                    Int.MAX_VALUE
                )
                addTriple(
                    rdf.createTriple(
                        namedNode,
                        valueKey,
                        rdf.createTypedString("tel:${newPhoneNumber}", null)
                    )
                )
                return true
            }
        } else {
            return false
        }
    }

    fun getEmails(): List<Email> {
        val returnList = arrayListOf<Email>()
        val list = dataset.defaultGraph.toList()
        list.filter { it.subject.value == getIdentifier().toString() && it.predicate.equals(hasEmailKey) }
            .forEach { triple ->
                returnList.add(
                    Email(
                        list.find { it.subject.equals(triple.`object`) && it.predicate.equals(valueKey) }!!.`object`.value
                    )
                )
            }

        return returnList
    }

    fun addEmailAddress(newEmailAddress: String?): Boolean {
        if (!newEmailAddress.isNullOrEmpty()) {
            val alreadyExistEmailAddress = dataset.defaultGraph.toList()
                .find { it.predicate.equals(valueKey) && it.`object`.value == "mailto:${newEmailAddress}" }

            return if (alreadyExistEmailAddress != null) {
                false
            } else {
                val namedNode = rdf.createBlankNode(newEmailAddress)
                addTriple(
                    rdf.createTriple(
                        rdf.createIRI(getIdentifier().toString()),
                        hasEmailKey,
                        namedNode
                    ),
                    Int.MAX_VALUE
                )
                addTriple(
                    rdf.createTriple(
                        namedNode,
                        valueKey,
                        rdf.createTypedString("mailto:${newEmailAddress}", null)
                    )
                )
                true
            }
        } else {
            return false
        }
    }

    fun removePhoneNumber(phoneNumber: String): Boolean {
        val list = dataset.defaultGraph.toList()
        val existingPhoneNumber = list.find { it.predicate.equals(valueKey) && it.`object`.value == "tel:${phoneNumber}" }

        if (existingPhoneNumber != null) {
            val hasTelephoneTripe = list.find { it.`object`.equals(existingPhoneNumber.subject) && it.predicate.equals(hasTelephoneKey) }
            dataset = rdf.createDataset().apply {
                list.filter { it != existingPhoneNumber && it != hasTelephoneTripe }.forEach {
                    add(it)
                }
            }
            return true
        } else {
            return false
        }
    }

    fun removeEmailAddress(emailAddress: String): Boolean {
        val list = dataset.defaultGraph.toList()
        val existingEmailAddress = list.find { it.predicate.equals(valueKey) && it.`object`.value == "mailto:${emailAddress}" }

        if (existingEmailAddress != null) {
            val hasEmailTriple = list.find { it.`object`.equals(existingEmailAddress.subject) && it.predicate.equals(hasEmailKey) }
            dataset = rdf.createDataset().apply {
                list.filter { it != existingEmailAddress && it != hasEmailTriple }.forEach {
                    add(it)
                }
            }
            return true
        } else {
            return false
        }
    }
}