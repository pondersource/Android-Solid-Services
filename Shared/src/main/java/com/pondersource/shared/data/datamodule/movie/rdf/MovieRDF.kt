package com.pondersource.shared.data.datamodule.movie.rdf

import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.RdfDataset
import com.inrupt.client.vocabulary.RDF
import com.pondersource.shared.RDFSource
import com.pondersource.shared.vocab.ExtendedXsdConstants
import com.pondersource.shared.vocab.DC
import com.pondersource.shared.vocab.Schema
import okhttp3.Headers
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date


class MovieRDF: RDFSource {

    companion object {
        private const val MOVIE_OBJ = Schema.Movie
    }
    private val rdfType = rdf.createIRI(RDF.type.toString())
    private val created = rdf.createIRI(DC.created)
    private val modified = rdf.createIRI(DC.modified)
    private val datePublished = rdf.createIRI(Schema.datePublished)
    private val description = rdf.createIRI(Schema.description)
    private val image = rdf.createIRI(Schema.image)
    private val name = rdf.createIRI(Schema.name)
    private val sameAs = rdf.createIRI(Schema.sameAs)

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

    constructor(
        identifier: URI,
        mediaType: MediaType,
        dataset: RdfDataset?,
        headers: Headers?
    ) : super(identifier, mediaType, dataset, headers)

    init {
        setType()
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
            ExtendedXsdConstants.dateTime
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
            ExtendedXsdConstants.dateTime
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
            ExtendedXsdConstants.dateTime
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