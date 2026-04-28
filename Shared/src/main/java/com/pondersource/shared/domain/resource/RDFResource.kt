package com.pondersource.shared.domain.resource

import android.os.Parcel
import android.os.Parcelable
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.jsonld.serialization.QuadsToJsonld
import com.apicatalog.rdf.api.RdfQuadConsumer
import com.pondersource.shared.vocab.ACL
import com.pondersource.shared.vocab.ACP
import com.pondersource.shared.vocab.AS
import com.pondersource.shared.vocab.Cert
import com.pondersource.shared.vocab.DC
import com.pondersource.shared.vocab.FOAF
import com.pondersource.shared.vocab.LDP
import com.pondersource.shared.vocab.Notify
import com.pondersource.shared.vocab.OWL
import com.pondersource.shared.vocab.SAI
import com.pondersource.shared.vocab.ShapeTree
import com.pondersource.shared.vocab.PIM
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.RDFS
import com.pondersource.shared.vocab.STAT
import com.pondersource.shared.vocab.Schema
import com.pondersource.shared.vocab.Solid
import com.pondersource.shared.vocab.VCARD
import com.pondersource.shared.vocab.XSD
import jakarta.json.spi.JsonProvider
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI

open class RDFResource : Resource {

    private val identifier: URI
    private val headers: Headers
    private val mediaType: MediaType
    protected var quads: MutableList<RdfQuad>
    protected val itselfSubject: String
    protected val contextDocument: JsonDocument? = JsonDocument.of(
        MediaType.JSON,
        JsonProvider.provider().createObjectBuilder().apply {
            add("rdf", RDF.NAMESPACE)
            add("rdfs", RDFS.NAMESPACE)
            add("owl", OWL.NAMESPACE)
            add("xsd", XSD.NAMESPACE)
            add("ldp", LDP.NAMESPACE)
            add("dc", DC.NAMESPACE)
            add("dcterms", DC.NAMESPACE)
            add("solid", Solid.NAMESPACE)
            add("foaf", FOAF.NAMESPACE)
            add("pim", PIM.NAMESPACE)
            add("acl", ACL.NAMESPACE)
            add("acp", ACP.NAMESPACE)
            add("cert", Cert.NAMESPACE)
            add("as", AS.NAMESPACE)
            add("notify", Notify.NAMESPACE)
            add("schema", Schema.NAMESPACE)
            add("interop", SAI.NAMESPACE)
            add("st", ShapeTree.NAMESPACE)
            add("stat", STAT.NAMESPACE)
            add("vcard", VCARD.NAMESPACE)
        }.build()
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RDFResource> {
            override fun createFromParcel(parcel: Parcel): RDFResource = RDFResource(parcel)
            override fun newArray(size: Int): Array<RDFResource?> = Array(size) { null }
        }

        fun parseJsonLd(
            document: JsonDocument,
            options: JsonLdOptions? = null
        ): MutableList<RdfQuad> {
            val result = mutableListOf<RdfQuad>()
            val api = JsonLd.toRdf(document)
            if (options != null) api.options(options)
            api.provide(object : RdfQuadConsumer {
                override fun quad(
                    subject: String, predicate: String, `object`: String,
                    datatype: String?, language: String?, direction: String?, graph: String?
                ): RdfQuadConsumer {
                    result.add(RdfQuad(subject, predicate, `object`, datatype, language, graph))
                    return this
                }
            })
            return result
        }
    }

    protected constructor(inParcel: Parcel) {
        this.identifier = URI.create(inParcel.readString())
        val headersMap = Json.decodeFromString<Map<String, List<String>>>(inParcel.readString()!!)
        this.headers = Headers.Builder().apply {
            headersMap.forEach { (name, values) -> values.forEach { add(name, it) } }
        }.build()
        this.mediaType = MediaType.of(inParcel.readString()!!)
        this.quads = parseJsonLd(
            JsonDocument.of(inParcel.readString()!!.byteInputStream()),
            JsonLdOptions().apply { isRdfStar = true }
        )
        this.itselfSubject = inParcel.readString()!!
    }

    constructor(identifier: URI) : this(identifier, null as List<RdfQuad>?)

    constructor(identifier: URI, quads: List<RdfQuad>?) :
        this(identifier, quads, null)

    constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
        this(identifier, MediaType.JSON_LD, quads, headers)

    constructor(identifier: URI, mediaType: MediaType, quads: List<RdfQuad>?) :
        this(identifier, mediaType, quads, null)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) {
        this.identifier    = identifier
        this.headers       = headers ?: Headers.Builder().build()
        this.mediaType     = mediaType
        this.quads         = quads?.toMutableList() ?: mutableListOf()
        this.itselfSubject = "$identifier#it"
    }

    fun addQuad(
        subject: String,
        predicate: String,
        obj: String,
        maxNumber: Int = 1
    ) {
        addQuadLiteral(subject, predicate, obj, null, null, maxNumber)
    }

    fun addQuadLiteral(
        subject: String,
        predicate: String,
        value: String,
        datatype: String? = XSD.STRING,
        language: String? = null,
        maxNumber: Int = 1
    ) {
        val quad = RdfQuad(subject, predicate, value, datatype, language)
        val current = quads.filter { it.subject == subject && it.predicate == predicate }
        if (current.size < maxNumber) {
            quads.add(quad)
            return
        }
        quads.removeAll { it.subject == subject && it.predicate == predicate }
        quads.add(quad)
    }

    fun clearProperties(predicate: String, subject: String = itselfSubject) {
        quads.removeAll { it.subject == subject && it.predicate == predicate }
    }

    fun findAllProperties(predicate: String): List<String> =
        quads.filter { it.predicate == predicate }.map { it.`object` }

    fun findProperty(predicate: String): String? =
        quads.find { it.predicate == predicate }?.`object`

    fun findAllPropertiesForSubject(subject: String, predicate: String): List<String> =
        quads.filter { it.subject == subject && it.predicate == predicate }.map { it.`object` }

    fun findPropertyForSubject(subject: String, predicate: String): String? =
        quads.find { it.subject == subject && it.predicate == predicate }?.`object`

    fun getAllQuads(): List<RdfQuad> = quads.toList()

    override fun getIdentifier(): URI = identifier

    override fun getContentType(): String = mediaType.toString()

    override fun getHeaders(): Headers = headers

    override fun getEntity(): InputStream {
        val converter = QuadsToJsonld()
        quads.forEach { q ->
            converter.quad(q.subject, q.predicate, q.`object`, q.datatype, q.language, null, q.graph)
        }
        val jsonLdArray = converter.toJsonLd()
        val compacted = JsonLd.compact(
            JsonDocument.of(jsonLdArray.toString().byteInputStream()),
            contextDocument
        ).get()
        return compacted.toString().byteInputStream()
    }

    override fun close() {
        try {
            getEntity().close()
        } catch (e: IOException) {
            throw UncheckedIOException("Unable to close RDFSource entity.", e)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier.toString())
        dest.writeString(Json.encodeToString(headers.toMultimap()))
        dest.writeString(mediaType.toString())
        val converter = QuadsToJsonld()
        quads.forEach { q ->
            converter.quad(q.subject, q.predicate, q.`object`, q.datatype, q.language, null, q.graph)
        }
        dest.writeString(converter.toJsonLd().toString())
        dest.writeString(itselfSubject)
    }
}
