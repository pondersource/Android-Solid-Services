package com.pondersource.shared.domain.profile

import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.domain.resource.RdfQuad
import com.pondersource.shared.domain.resource.SolidRDFResource
import com.pondersource.shared.domain.util.toPlainString
import com.pondersource.shared.vocab.ACL
import com.pondersource.shared.vocab.Cert
import com.pondersource.shared.vocab.FOAF
import com.pondersource.shared.vocab.LDP
import com.pondersource.shared.vocab.PIM
import com.pondersource.shared.vocab.RDF
import com.pondersource.shared.vocab.RDFS
import com.pondersource.shared.vocab.Solid
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.Headers
import java.net.URI

/**
 * Represents a Solid WebID identity document — both for reading profile data
 * and for constructing or updating it.
 *
 * A WebID document is an RDF resource that describes an agent. It MUST contain:
 * - `rdf:type foaf:Agent`
 * - `pim:preferencesFile` pointing to the agent's preferences file
 *
 * It MAY contain:
 * - `pim:storage` (one or more storage roots)
 * - `ldp:inbox` (notification inbox)
 * - `rdfs:seeAlso` (extended profile documents)
 * - `foaf:isPrimaryTopicOf`
 * - `solid:oidcIssuer` (OIDC identity provider)
 * - FOAF name / given name / family name / photo
 * - `foaf:knows` (social graph)
 * - `acl:trustedApp` (trusted client applications)
 * - `cert:key` (public key for WebID-TLS)
 *
 * Use [getMetadata] (from [SolidRDFResource]) to access the ETag, ACL URI, and
 * allowed methods returned by the server — useful for conditional PUT updates.
 *
 * Spec: https://solid.github.io/webid-profile/
 */
open class WebId : SolidRDFResource {

    companion object {
        private const val KEY_IDENTIFIER = "identifier"
        private const val KEY_TYPE = "type"
        private const val KEY_DATASET = "dataset"

        fun writeToString(webId: WebId?): String? {
            webId ?: return null
            return buildJsonObject {
                put(KEY_IDENTIFIER, webId.getIdentifier().toString())
                put(KEY_TYPE, webId.getContentType())
                put(KEY_DATASET, webId.getEntity().toPlainString())
            }.toString()
        }

        fun readFromString(objectString: String): WebId {
            val obj = Json.parseToJsonElement(objectString).jsonObject
            return WebId(
                identifier = URI.create(obj[KEY_IDENTIFIER]!!.jsonPrimitive.content),
                mediaType = MediaType.of(obj[KEY_TYPE]!!.jsonPrimitive.content),
                quads = parseJsonLd(
                    JsonDocument.of(obj[KEY_DATASET]!!.jsonPrimitive.content.byteInputStream())
                ),
            )
        }
    }

    constructor(identifier: URI, quads: List<RdfQuad>) :
            super(identifier, quads)

    constructor(identifier: URI, mediaType: MediaType, quads: List<RdfQuad>) :
            super(identifier, mediaType, quads, null)

    constructor(identifier: URI, mediaType: MediaType, quads: List<RdfQuad>, headers: Headers?) :
            super(identifier, mediaType, quads, headers)

    fun getTypes(): List<URI> =
        findAllProperties(RDF.TYPE).map { URI.create(it) }

    fun getPreferencesFile(): URI? =
        findProperty(PIM.PREFERENCES_FILE)?.let { runCatching { URI.create(it) }.getOrNull() }

    fun getStorages(): List<URI> =
        findAllProperties(PIM.STORAGE).map { URI.create(it) }

    fun getInbox(): URI? =
        findProperty(LDP.INBOX)?.let { runCatching { URI.create(it) }.getOrNull() }

    fun getRelatedResources(): List<URI> =
        findAllProperties(RDFS.SEE_ALSO).map { URI.create(it) }

    fun getPrimaryTopicDocuments(): List<URI> =
        findAllProperties(FOAF.IS_PRIMARY_TOPIC_OF).map { URI.create(it) }

    fun getOidcIssuers(): List<URI> =
        findAllProperties(Solid.OIDC_ISSUER).map { URI.create(it) }

    fun getPrivateTypeIndex(): URI? =
        findProperty(Solid.PRIVATE_TYPE_INDEX)?.let { runCatching { URI.create(it) }.getOrNull() }

    fun getPublicTypeIndex(): URI? =
        findProperty(Solid.PUBLIC_TYPE_INDEX)?.let { runCatching { URI.create(it) }.getOrNull() }

    fun getName(): String? = findProperty(FOAF.NAME)

    fun getGivenName(): String? = findProperty(FOAF.GIVEN_NAME)

    fun getFamilyName(): String? = findProperty(FOAF.FAMILY_NAME)

    fun getPhoto(): URI? =
        findProperty(FOAF.IMG)?.let { runCatching { URI.create(it) }.getOrNull() }

    fun getKnows(): List<URI> =
        findAllProperties(FOAF.KNOWS).map { URI.create(it) }

    fun getTrustedApps(): List<URI> =
        findAllProperties(ACL.TRUSTED_APP)
            .mapNotNull { runCatching { URI.create(it) }.getOrNull() }

    fun getCertKeys(): List<URI> =
        findAllProperties(Cert.KEY)
            .mapNotNull { runCatching { URI.create(it) }.getOrNull() }

    fun setPrivateTypeIndex(
        webId: String,
        storage: String,
        uri: String = "$storage/settings/privateTypeIndex",
    ) {
        addQuad(webId, Solid.PRIVATE_TYPE_INDEX, uri)
    }

    fun setPublicTypeIndex(
        webId: String,
        storage: String,
        uri: String = "$storage/settings/publicTypeIndex",
    ) {
        addQuad(webId, Solid.PUBLIC_TYPE_INDEX, uri)
    }

    fun setPreferencesFile(
        webId: String,
        storage: String,
        uri: String = "$storage/settings/prefs.ttl",
    ) {
        addQuad(webId, PIM.PREFERENCES_FILE, uri)
    }

    fun setStorage(webId: String, storageUri: String) {
        addQuad(webId, PIM.STORAGE, storageUri, maxNumber = Int.MAX_VALUE)
    }

    fun setInbox(
        webId: String,
        storage: String,
        uri: String = "$storage/inbox/",
    ) {
        addQuad(webId, LDP.INBOX, uri)
    }

    fun setOidcIssuer(webId: String, issuerUri: String) {
        addQuad(webId, Solid.OIDC_ISSUER, issuerUri, maxNumber = Int.MAX_VALUE)
    }
}
