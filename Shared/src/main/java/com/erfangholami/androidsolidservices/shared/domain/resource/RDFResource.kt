package com.erfangholami.androidsolidservices.shared.domain.resource

import android.os.Parcel
import android.os.Parcelable
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.JsonLdOptions
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.jsonld.serialization.QuadsToJsonld
import com.apicatalog.jsonld.uri.UriValidationPolicy
import com.apicatalog.rdf.api.RdfQuadConsumer
import com.erfangholami.androidsolidservices.shared.domain.util.encodeUri
import com.erfangholami.androidsolidservices.shared.domain.util.encodeUriString
import com.erfangholami.androidsolidservices.shared.vocab.ACL
import com.erfangholami.androidsolidservices.shared.vocab.ACP
import com.erfangholami.androidsolidservices.shared.vocab.AS
import com.erfangholami.androidsolidservices.shared.vocab.Cert
import com.erfangholami.androidsolidservices.shared.vocab.DC
import com.erfangholami.androidsolidservices.shared.vocab.FOAF
import com.erfangholami.androidsolidservices.shared.vocab.LDP
import com.erfangholami.androidsolidservices.shared.vocab.Notify
import com.erfangholami.androidsolidservices.shared.vocab.OWL
import com.erfangholami.androidsolidservices.shared.vocab.PIM
import com.erfangholami.androidsolidservices.shared.vocab.RDF
import com.erfangholami.androidsolidservices.shared.vocab.RDFS
import com.erfangholami.androidsolidservices.shared.vocab.SAI
import com.erfangholami.androidsolidservices.shared.vocab.STAT
import com.erfangholami.androidsolidservices.shared.vocab.Schema
import com.erfangholami.androidsolidservices.shared.vocab.ShapeTree
import com.erfangholami.androidsolidservices.shared.vocab.Solid
import com.erfangholami.androidsolidservices.shared.vocab.VCARD
import com.erfangholami.androidsolidservices.shared.vocab.XSD
import jakarta.json.spi.JsonProvider
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okio.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI

public open class RDFResource : Resource {

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

    public companion object {
        @JvmField
        public val CREATOR: Parcelable.Creator<RDFResource> = object : Parcelable.Creator<RDFResource> {
            override fun createFromParcel(parcel: Parcel): RDFResource = RDFResource(parcel)
            override fun newArray(size: Int): Array<RDFResource?> = Array(size) { null }
        }

        public fun parseJsonLd(
            document: JsonDocument,
            options: JsonLdOptions = JsonLdOptions().apply {
                uriValidation = UriValidationPolicy.SchemeOnly
            }
        ): MutableList<RdfQuad> {
            val result = mutableListOf<RdfQuad>()
            val api = JsonLd.toRdf(document)
            api.options(options)
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
        this.identifier = encodeUriString(inParcel.readString()!!)
        val headersMap = Json.decodeFromString<Map<String, List<String>>>(inParcel.readString()!!)
        this.headers = Headers.Builder().apply {
            headersMap.forEach { (name, values) -> values.forEach { add(name, it) } }
        }.build()
        this.mediaType = MediaType.of(inParcel.readString()!!)
        this.quads = parseJsonLd(
            JsonDocument.of(inParcel.readString()!!.byteInputStream()),
        )
        this.itselfSubject = inParcel.readString()!!
    }

    public constructor(identifier: URI) : this(identifier, null as List<RdfQuad>?)

    public constructor(identifier: URI, quads: List<RdfQuad>?) :
            this(identifier, quads, null)

    public constructor(identifier: URI, quads: List<RdfQuad>?, headers: Headers?) :
            this(identifier, MediaType.JSON_LD, quads, headers)

    public constructor(identifier: URI, mediaType: MediaType, quads: List<RdfQuad>?) :
            this(identifier, mediaType, quads, null)

    public constructor(
        identifier: URI,
        mediaType: MediaType,
        quads: List<RdfQuad>?,
        headers: Headers?
    ) {
        this.identifier = encodeUri(identifier)
        this.headers = headers ?: Headers.Builder().build()
        this.mediaType = mediaType
        this.quads = quads?.toMutableList() ?: mutableListOf()
        this.itselfSubject = "$identifier#it"
    }

    public fun addQuad(
        subject: String,
        predicate: String,
        obj: String,
        maxNumber: Int = 1
    ) {
        addQuadLiteral(subject, predicate, obj, null, null, maxNumber)
    }

    public fun addQuadLiteral(
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

    public fun clearProperties(predicate: String, subject: String = itselfSubject) {
        quads.removeAll { it.subject == subject && it.predicate == predicate }
    }

    public fun findAllProperties(predicate: String): List<String> =
        quads.filter { it.predicate == predicate }.map { it.`object` }

    public fun findProperty(predicate: String): String? =
        quads.find { it.predicate == predicate }?.`object`

    public fun findAllPropertiesForSubject(subject: String, predicate: String): List<String> =
        quads.filter { it.subject == subject && it.predicate == predicate }.map { it.`object` }

    public fun findPropertyForSubject(subject: String, predicate: String): String? =
        quads.find { it.subject == subject && it.predicate == predicate }?.`object`

    public fun getAllQuads(): List<RdfQuad> = quads.toList()

    override fun getIdentifier(): URI = identifier

    override fun getContentType(): String = mediaType.toString()

    override fun getHeaders(): Headers = headers

    override fun getEntity(): InputStream {
        val converter = QuadsToJsonld()
        quads.forEach { q ->
            converter.quad(
                q.subject,
                q.predicate,
                q.`object`,
                q.datatype,
                q.language,
                null,
                q.graph
            )
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
            converter.quad(
                q.subject,
                q.predicate,
                q.`object`,
                q.datatype,
                q.language,
                null,
                q.graph
            )
        }
        dest.writeString(converter.toJsonLd().toString())
        dest.writeString(itselfSubject)
    }
}
