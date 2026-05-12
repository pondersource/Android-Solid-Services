package com.erfangholami.androidsolidservices.api.sharing.implementation

import com.apicatalog.jsonld.http.media.MediaType
import com.erfangholami.androidsolidservices.shared.domain.network.HTTPAcceptType
import com.erfangholami.androidsolidservices.shared.domain.profile.WebId
import com.erfangholami.androidsolidservices.shared.domain.resource.SolidNonRDFResource
import com.erfangholami.androidsolidservices.shared.domain.sharing.ProfileField
import com.erfangholami.androidsolidservices.shared.domain.sharing.ProfileShareConfig
import com.erfangholami.androidsolidservices.shared.domain.sharing.rdf.ProfileSnapshotRDF
import com.erfangholami.androidsolidservices.api.resource.SolidResourceManager
import java.net.URI

/**
 * Reads the owner's WebID profile and assembles two artifacts at [snapshotUri]:
 *
 * 1. A small RDF snapshot containing only the [ProfileShareConfig.selectedFields]
 *    (`{snapshotUri}#me`) plus a `dcterms:creator` triple back to the owner.
 * 2. A self-contained HTML "business card" at the sibling `.html` URL so a
 *    non-Solid recipient can open the QR target directly in a browser.
 */
internal class ProfileSnapshotBuilder(private val rm: SolidResourceManager) {

    suspend fun build(
        webId: String,
        config: ProfileShareConfig,
        snapshotUri: URI,
    ): ProfileSnapshotRDF {
        val profile = rm.read(webId, URI.create(webId), WebId::class.java).getOrThrow()
        val snapshot = ProfileSnapshotRDF(
            identifier = snapshotUri,
            mediaType = MediaType.JSON_LD,
        ).apply {
            markAsAgent()
            setOwner(webId)
        }
        config.selectedFields.forEach { field ->
            populate(snapshot, profile, field, ownerWebId = webId)
        }
        return snapshot
    }

    fun buildHtml(
        webId: String,
        config: ProfileShareConfig,
        snapshot: ProfileSnapshotRDF,
        htmlUri: URI,
    ): SolidNonRDFResource {
        val html = renderHtml(webId, snapshot, config.selectedFields)
        return SolidNonRDFResource(
            identifier = htmlUri,
            contentType = HTTPAcceptType.TEXT_HTML,
            entity = html.byteInputStream(),
        )
    }

    private fun populate(
        snapshot: ProfileSnapshotRDF,
        profile: WebId,
        field: ProfileField,
        ownerWebId: String,
    ) {
        when (field) {
            ProfileField.NAME -> profile.getName()?.let { snapshot.setLiteral(field, it) }
            ProfileField.GIVEN_NAME -> profile.getGivenName()?.let { snapshot.setLiteral(field, it) }
            ProfileField.FAMILY_NAME -> profile.getFamilyName()?.let { snapshot.setLiteral(field, it) }
            ProfileField.WEBID -> snapshot.setIri(field, ownerWebId)
            ProfileField.AVATAR -> profile.getPhoto()?.let { snapshot.setIri(field, it.toString()) }
            ProfileField.OIDC_ISSUER -> profile.getOidcIssuers().firstOrNull()
                ?.let { snapshot.setIri(field, it.toString()) }

            ProfileField.EMAIL -> profile.findAllProperties(field.predicate)
                .forEach { snapshot.setLiteral(field, it) }

            ProfileField.PHONE -> profile.findAllProperties(field.predicate)
                .forEach { snapshot.setLiteral(field, it) }

            ProfileField.ORGANIZATION -> profile.findProperty(field.predicate)
                ?.let { snapshot.setLiteral(field, it) }

            ProfileField.ROLE -> profile.findProperty(field.predicate)
                ?.let { snapshot.setLiteral(field, it) }
        }
    }

    private fun renderHtml(
        ownerWebId: String,
        snapshot: ProfileSnapshotRDF,
        fields: Set<ProfileField>,
    ): String {
        val name = snapshot.getLiteral(ProfileField.NAME)
            ?: snapshot.getLiteral(ProfileField.GIVEN_NAME)
            ?: ownerWebId
        val avatar = snapshot.getIri(ProfileField.AVATAR)
        val rows = fields
            .filter { it != ProfileField.NAME && it != ProfileField.AVATAR }
            .mapNotNull { f ->
                val value = when (f) {
                    ProfileField.WEBID -> ownerWebId
                    else -> snapshot.getLiteral(f) ?: snapshot.getIri(f)
                }
                value?.let { f.label to it }
            }

        val esc = { s: String ->
            s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
        }

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\"><head>")
            appendLine("<meta charset=\"utf-8\"/>")
            appendLine("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>")
            appendLine("<title>${esc(name)} — Solid Profile</title>")
            appendLine("<style>")
            appendLine(BUSINESS_CARD_CSS)
            appendLine("</style></head><body>")
            appendLine("<main class=\"card\">")
            if (avatar != null) {
                appendLine(
                    "<img class=\"avatar\" src=\"${esc(avatar)}\" alt=\"${esc(name)}\"/>",
                )
            } else {
                appendLine("<div class=\"avatar placeholder\">${esc(name.first().toString())}</div>")
            }
            appendLine("<h1>${esc(name)}</h1>")
            appendLine("<dl>")
            rows.forEach { (label, value) ->
                val display = if (value.startsWith("http") || value.startsWith("mailto:")) {
                    "<a href=\"${esc(value)}\">${esc(value.removePrefix("mailto:"))}</a>"
                } else esc(value)
                appendLine("<dt>${esc(label)}</dt><dd>$display</dd>")
            }
            appendLine("</dl>")
            appendLine("<footer>Shared via Solid Share — <a href=\"${esc(ownerWebId)}\">${esc(ownerWebId)}</a></footer>")
            appendLine("</main>")
            appendLine("</body></html>")
        }
    }

    private companion object {
        // Lifted into a constant so the HTML output stays readable.
        const val BUSINESS_CARD_CSS = """
:root { color-scheme: light dark; }
* { box-sizing: border-box; }
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.card {
  background: white;
  color: #1f2937;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0,0,0,.25);
  padding: 32px;
  width: 100%;
  max-width: 420px;
  text-align: center;
}
.avatar {
  width: 120px; height: 120px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto 16px;
  display: block;
  background: #e5e7eb;
}
.avatar.placeholder {
  display: flex; align-items: center; justify-content: center;
  font-size: 48px; font-weight: 600; color: #6366f1;
  background: #eef2ff;
}
h1 { margin: 0 0 24px; font-size: 24px; font-weight: 600; }
dl { text-align: left; margin: 0; }
dt {
  font-size: 12px; text-transform: uppercase; letter-spacing: .04em;
  color: #6b7280; margin-top: 12px;
}
dd { margin: 0; padding: 4px 0 0; word-break: break-word; }
dd a { color: #6366f1; text-decoration: none; }
dd a:hover { text-decoration: underline; }
footer {
  margin-top: 24px; padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  font-size: 11px; color: #6b7280; word-break: break-all;
}
footer a { color: #6b7280; }
@media (prefers-color-scheme: dark) {
  .card { background: #1f2937; color: #f9fafb; }
  dt { color: #9ca3af; }
  footer { border-color: #374151; color: #9ca3af; }
}
"""
    }
}
