package com.pondersource.solidandroidclient

import android.os.Parcel
import android.os.Parcelable
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.document.RdfDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.apicatalog.rdf.RdfResource
import com.apicatalog.rdf.RdfTriple
import com.apicatalog.rdf.RdfValue
import com.apicatalog.rdf.impl.DefaultRdfProvider
import com.apicatalog.rdf.spi.RdfProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pondersource.solidandroidclient.sub.resource.Resource
import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI

open class RDFSource : Resource {

    protected val rdf = RdfProvider.provider()

    private val identifier: URI
    private val headers: Headers
    private val mediaType: MediaType
    protected var dataset: RdfDataset
    
    private val itselfSubject : String

    companion object {
        @JvmField
        val CREATOR = object: Parcelable.Creator<RDFSource>
        {
            override fun createFromParcel(parcel: Parcel): RDFSource {
                return RDFSource(parcel)
            }

            override fun newArray(size: Int): Array<RDFSource?> {
                return Array(size) { null }
            }
        }
    }

    private constructor(inParcel: Parcel) {
        this.identifier = URI.create(inParcel.readString())
        this.headers = Gson().fromJson<Headers>(inParcel.readString(), object : TypeToken<Headers>() {}.type)
        this.mediaType = Gson().fromJson<MediaType>(inParcel.readString(), object : TypeToken<MediaType>() {}.type)
        this.dataset = JsonLd.toRdf(JsonDocument.of(inParcel.readString()!!.byteInputStream())).options(JsonLdOptions().apply {
            isRdfStar = true
        }).get()
        this.itselfSubject = inParcel.readString()!!
    }

    constructor(identifier: URI) : this(identifier, null)

    constructor(
        identifier: URI,
        dataset: RdfDataset?
    ) : this(identifier, dataset, null)

    constructor(
        identifier: URI,
        dataset: RdfDataset?,
        headers: Headers?
    ): this(identifier, MediaType.JSON_LD, dataset, headers)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
    ): this(identifier, mediaType, dataset, null)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ) {
        this.identifier = identifier
        this.headers = headers ?: Headers.Builder().build()
        this.mediaType = mediaType
        this.dataset = dataset ?: DefaultRdfProvider().createDataset()
        
        this.itselfSubject = "$identifier#it"
    }

    fun createTriple(predicate: RdfResource, objectValue: String, objectType: String? = null): RdfTriple {
        return rdf.createTriple(
            rdf.createIRI(itselfSubject),
            predicate,
            rdf.createTypedString(objectValue, objectType)
        )
    }

    fun addTriple(triple: RdfTriple, maxNumber: Int = 1) {

        val currentItemSize = dataset.defaultGraph.toList().filter {
            it.subject.equals(triple.subject) && it.predicate.equals(triple.predicate)
        }.size

        if(currentItemSize < maxNumber) {
            dataset.add(triple)
            return
        } else {
            val all = dataset.defaultGraph.toList()

            val newDateset = rdf.createDataset()

            all.forEach {
                if (!it.subject.equals(triple.subject) || !it.predicate.equals(triple.predicate)) {
                    newDateset.add(it)
                }
            }

            newDateset.add(triple)

            dataset = newDateset
        }
    }

    fun clearProperties(
        predicate: String,
        subject: String = itselfSubject
    ) {
        val allWithProperties = dataset.defaultGraph.toList().filter {
            it.subject.value == subject && it.predicate.value == predicate
        }

        if (allWithProperties.isNotEmpty()) {
            val all = dataset.defaultGraph.toList()
            all.removeAll(allWithProperties)

            val newDateset = rdf.createDataset()

            all.forEach {
                newDateset.add(it)
            }
            dataset = newDateset
        } else {
            //No need to clear as we already know it doesn't exist
        }
    }

    fun findAllProperties(predicate: RdfResource): List<RdfValue> {
        return dataset.defaultGraph.toList()
            .filter{ it.predicate.value == predicate.value }
            .map { it.`object` }
    }

    fun findProperty(predicate: RdfResource): RdfValue? {
        return dataset.defaultGraph.toList()
            .find{ it.predicate.value == predicate.value }
            ?.`object`
    }

    override fun getIdentifier(): URI = identifier

    override fun getContentType(): String = mediaType.toString()

    override fun getHeaders(): Headers = headers

    override fun getEntity(): InputStream {
        val toBeCompactDoc = JsonDocument.of(JsonLd.fromRdf(RdfDocument.of(dataset)).get().toString().byteInputStream())
        val contextDoc = JsonDocument.of("{}".byteInputStream())
        val compacted = JsonLd.compact(toBeCompactDoc, contextDoc).get()
        return compacted.toString().byteInputStream()
    }

    override fun close() {
        try {
            getEntity().close()
        } catch (e: IOException) {
            throw UncheckedIOException("Unable to close RDFSource entity.", e)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier.toString())
        dest.writeString(Gson().toJson(headers))
        dest.writeString(Gson().toJson(mediaType))
        dest.writeString(JsonLd.fromRdf(RdfDocument.of(dataset)).get().toString())
        dest.writeString(itselfSubject)
    }
}