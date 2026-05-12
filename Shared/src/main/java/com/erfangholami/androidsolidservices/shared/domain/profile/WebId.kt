package com.erfangholami.androidsolidservices.shared.domain.profile

import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.resource.RdfQuad
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidRDFResource
import com.erfangholami.androidsolidservices.shared.domain.util.toPlainString
import com.erfangholami.androidsolidservices.shared.vocab.ACL
import com.erfangholami.androidsolidservices.shared.vocab.Cert
import com.erfangholami.androidsolidservices.shared.vocab.FOAF
import com.erfangholami.androidsolidservices.shared.vocab.LDP
import com.erfangholami.androidsolidservices.shared.vocab.PIM
import com.erfangholami.androidsolidservices.shared.vocab.RDF
import com.erfangholami.androidsolidservices.shared.vocab.RDFS
import com.erfangholami.androidsolidservices.shared.vocab.Solid
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
public open class WebId : SolidRDFResource {

    public companion object {
        private const val KEY_IDENTIFIER = "identifier"
        private const val KEY_TYPE = "type"
        private const val KEY_DATASET = "dataset"

        public fun writeToString(webId: WebId?): String? {
            webId ?: return null
            return buildJsonObject {
                put(KEY_IDENTIFIER, webId.getIdentifier().toString())
                put(KEY_TYPE, webId.getContentType())
                put(KEY_DATASET, webId.getEntity().toPlainString())
            }.toString()
        }

        public fun readFromString(objectString: String): WebId {
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

    public constructor(identifier: URI, quads: List<RdfQuad>) :
            super(identifier, quads)

    public constructor(identifier: URI, mediaType: MediaType, quads: List<RdfQuad>) :
            super(identifier, mediaType, quads, null)

    public constructor(identifier: URI, mediaType: MediaType, quads: List<RdfQuad>, headers: Headers?) :
            super(identifier, mediaType, quads, headers)

    public fun getTypes(): List<URI> =
        findAllProperties(RDF.TYPE).map { URI.create(it) }

    public fun getPreferencesFile(): URI? =
        findProperty(PIM.PREFERENCES_FILE)?.let { runCatching { URI.create(it) }.getOrNull() }

    public fun getStorages(): List<URI> =
        findAllProperties(PIM.STORAGE).map { URI.create(it) }

    public fun getInbox(): URI? =
        findProperty(LDP.INBOX)?.let { runCatching { URI.create(it) }.getOrNull() }

    public fun getRelatedResources(): List<URI> =
        findAllProperties(RDFS.SEE_ALSO).map { URI.create(it) }

    public fun getPrimaryTopicDocuments(): List<URI> =
        findAllProperties(FOAF.IS_PRIMARY_TOPIC_OF).map { URI.create(it) }

    public fun getOidcIssuers(): List<URI> =
        findAllProperties(Solid.OIDC_ISSUER).map { URI.create(it) }

    public fun getPrivateTypeIndex(): URI? =
        findProperty(Solid.PRIVATE_TYPE_INDEX)?.let { runCatching { URI.create(it) }.getOrNull() }

    public fun getPublicTypeIndex(): URI? =
        findProperty(Solid.PUBLIC_TYPE_INDEX)?.let { runCatching { URI.create(it) }.getOrNull() }

    public fun getName(): String? = findProperty(FOAF.NAME)

    public fun getGivenName(): String? = findProperty(FOAF.GIVEN_NAME)

    public fun getFamilyName(): String? = findProperty(FOAF.FAMILY_NAME)

    public fun getPhoto(): URI? =
        findProperty(FOAF.IMG)?.let { runCatching { URI.create(it) }.getOrNull() }

    public fun getKnows(): List<URI> =
        findAllProperties(FOAF.KNOWS).map { URI.create(it) }

    public fun getTrustedApps(): List<URI> =
        findAllProperties(ACL.TRUSTED_APP)
            .mapNotNull { runCatching { URI.create(it) }.getOrNull() }

    public fun getCertKeys(): List<URI> =
        findAllProperties(Cert.KEY)
            .mapNotNull { runCatching { URI.create(it) }.getOrNull() }

    public fun setPrivateTypeIndex(
        webId: String,
        storage: String,
        uri: String = "$storage/settings/privateTypeIndex",
    ) {
        addQuad(webId, Solid.PRIVATE_TYPE_INDEX, uri)
    }

    public fun setPublicTypeIndex(
        webId: String,
        storage: String,
        uri: String = "$storage/settings/publicTypeIndex",
    ) {
        addQuad(webId, Solid.PUBLIC_TYPE_INDEX, uri)
    }

    public fun setPreferencesFile(
        webId: String,
        storage: String,
        uri: String = "$storage/settings/prefs.ttl",
    ) {
        addQuad(webId, PIM.PREFERENCES_FILE, uri)
    }

    public fun setStorage(webId: String, storageUri: String) {
        addQuad(webId, PIM.STORAGE, storageUri, maxNumber = Int.MAX_VALUE)
    }

    public fun setInbox(
        webId: String,
        storage: String,
        uri: String = "$storage/inbox/",
    ) {
        addQuad(webId, LDP.INBOX, uri)
    }

    public fun setOidcIssuer(webId: String, issuerUri: String) {
        addQuad(webId, Solid.OIDC_ISSUER, issuerUri, maxNumber = Int.MAX_VALUE)
    }
}
