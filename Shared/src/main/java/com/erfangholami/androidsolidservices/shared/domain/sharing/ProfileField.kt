package com.erfangholami.androidsolidservices.shared.domain.sharing

import com.erfangholami.androidsolidservices.shared.vocab.FOAF
import com.erfangholami.androidsolidservices.shared.vocab.Solid
import com.erfangholami.androidsolidservices.shared.vocab.VCARD

/**
 * A single WebID profile field that the owner can choose to include in a profile share.
 *
 * The [predicate] is the RDF predicate read from the owner's WebID profile;
 * the same predicate is written to the snapshot document so any consumer
 * (Solid client or HTML viewer) can interpret it.
 */
public enum class ProfileField(public val predicate: String, public val label: String) {
    NAME(FOAF.NAME, "Name"),
    GIVEN_NAME(FOAF.GIVEN_NAME, "Given name"),
    FAMILY_NAME(FOAF.FAMILY_NAME, "Family name"),
    WEBID(Solid.NAMESPACE + "webid", "WebID"),
    EMAIL(VCARD.HAS_EMAIL, "Email"),
    PHONE(VCARD.HAS_TELEPHONE, "Phone"),
    AVATAR(FOAF.IMG, "Avatar"),
    ORGANIZATION(VCARD.ORGANIZATION_NAME, "Organization"),
    ROLE(VCARD.ROLE, "Role"),
    OIDC_ISSUER(Solid.OIDC_ISSUER, "Solid Provider");

    public companion object {
        public fun fromPredicate(predicate: String): ProfileField? =
            entries.firstOrNull { it.predicate == predicate }
    }
}
