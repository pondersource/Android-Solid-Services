package com.pondersource.solidandroidclient.datamodules

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.solidandroidclient.lang.ExtendedXsdConstants
import com.pondersource.solidandroidclient.sub.resource.RDFSource
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date


class Movie: RDFSource {

    companion object {
        private const val MOVIE_OBJ = "https://schema.org/Movie"
    }
    private val rdfType = rdf.createIRI(RDF.type.toString())
    private val created = rdf.createIRI("http://purl.org/dc/terms/created")
    private val modified = rdf.createIRI("http://purl.org/dc/terms/modified")
    private val datePublished = rdf.createIRI("https://schema.org/datePublished")
    private val description = rdf.createIRI("https://schema.org/description")
    private val image = rdf.createIRI("https://schema.org/image")
    private val name = rdf.createIRI("https://schema.org/name")
    private val sameAs = rdf.createIRI("https://schema.org/sameAs")

    constructor(
        identifier: URI,
    ) : super(identifier)

    constructor(
        identifier: URI,
        dataset: RdfDataset,
    ) : super(identifier, dataset)

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset,
    ) : super(identifier, mediaType, dataset)

    init {
        if(dataset.size() == 0) {
            setType()
        }
    }

    fun getType(): URI {
        return URI.create(findProperty(rdfType)!!.value)
    }

    private fun setType() {
        val triplet = createTriple(
            rdfType,
            MOVIE_OBJ
        )
        addTriple(triplet, 1)
    }

    fun getCreatedTime(): LocalDateTime? {
        val created = findProperty(created)
        return if (created != null) {
            LocalDateTime.from(DateTimeFormatter.ISO_INSTANT.parse(created.value))
        } else {
            null
        }
    }

    fun updateCreatedTime(dateTime: Date) {

        val triplet = createTriple(
            created,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dateTime),
            ExtendedXsdConstants.DATE_TIME
        )
        addTriple(triplet, 1)
    }

    fun getLastModifiedTime(): LocalDateTime? {
        val modified = findProperty(modified)
        return if (modified != null) {
            LocalDateTime.from(DateTimeFormatter.ISO_INSTANT.parse(modified.value))
        } else {
            null
        }
    }

    fun setLastModificationTime(dateTime: Date) {
        val triplet = createTriple(
            modified,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dateTime),
            ExtendedXsdConstants.DATE_TIME
        )
        addTriple(triplet)
    }

    fun getPublishDate(): LocalDateTime? {
        val published = findProperty(datePublished)
        return if (published != null) {
            LocalDateTime.from(DateTimeFormatter.ISO_INSTANT.parse(published.value))
        } else {
            null
        }
    }

    fun setPublishDate(dateTime: Date) {
        val triplet = createTriple(
            datePublished,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dateTime),
            ExtendedXsdConstants.DATE_TIME
        )
        addTriple(triplet)
    }

    fun getDescription(): String? {
        return findProperty(description)?.value
    }

    fun setDescription(description: String) {
        val triplet = createTriple(
            this.description,
            description
        )
        addTriple(triplet)
    }

    fun getImageUrl(): URI? {
        val imageUrl = findProperty(image)
         return if (imageUrl != null) {
             URI.create(imageUrl.value)
         } else {
             null
         }
    }

    fun setImageUrl(imageUrl: URI) {
        val triplet = createTriple(
            this.image,
            imageUrl.toString()
        )
        addTriple(triplet)
    }

    fun getName(): String? {
        return findProperty(name)?.value
    }

    fun setName(name: String) {
        val triplet = createTriple(
            this.name,
            name
        )
        addTriple(triplet)
    }

    fun sameMovies(): List<URI> {
        return findAllProperties(sameAs)
            .map { URI.create(it.value) }
    }

    fun addSameMovie(sameMovie: URI) {
        val triplet = createTriple(
            this.sameAs,
            sameMovie.toString()
        )
        addTriple(triplet, Int.MAX_VALUE)
    }

    fun clearSameMovies() {
        clearProperties(sameAs.value)
    }

}