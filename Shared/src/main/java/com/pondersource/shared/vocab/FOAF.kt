package com.pondersource.shared.vocab

/**
 * Friend of a Friend (FOAF) vocabulary constants.
 * http://xmlns.com/foaf/0.1/
 */
object FOAF {
    const val NAMESPACE = "http://xmlns.com/foaf/0.1/"

    //Types
    const val AGENT = "${NAMESPACE}Agent"
    const val PERSON = "${NAMESPACE}Person"
    const val ORGANIZATION = "${NAMESPACE}Organization"
    const val DOCUMENT = "${NAMESPACE}Document"
    const val PERSONAL_PROFILE_DOCUMENT = "${NAMESPACE}PersonalProfileDocument"
    const val ONLINE_ACCOUNT = "${NAMESPACE}OnlineAccount"

    //Identity predicates
    const val NAME = "${NAMESPACE}name"
    const val GIVEN_NAME = "${NAMESPACE}givenName"
    const val FAMILY_NAME = "${NAMESPACE}familyName"
    const val TITLE = "${NAMESPACE}title"
    const val NICK = "${NAMESPACE}nick"
    const val MBOX = "${NAMESPACE}mbox"
    const val MBOX_SHA1_SUM = "${NAMESPACE}mbox_sha1sum"

    //Profile / social
    const val IMG = "${NAMESPACE}img"
    const val DEPICTION = "${NAMESPACE}depiction"
    const val HOMEPAGE = "${NAMESPACE}homepage"
    const val WEBLOG = "${NAMESPACE}weblog"
    const val ACCOUNT = "${NAMESPACE}account"
    const val KNOWS = "${NAMESPACE}knows"
    const val MEMBER = "${NAMESPACE}member"

    //Document predicates
    const val PRIMARY_TOPIC = "${NAMESPACE}primaryTopic"
    const val IS_PRIMARY_TOPIC_OF = "${NAMESPACE}isPrimaryTopicOf"
    const val MAKER = "${NAMESPACE}maker"
    const val MADE = "${NAMESPACE}made"
    const val TOPIC = "${NAMESPACE}topic"
    const val PAGE = "${NAMESPACE}page"

    //Account predicates
    const val ACCOUNT_NAME = "${NAMESPACE}accountName"
    const val ACCOUNT_SERVICE_HOMEPAGE = "${NAMESPACE}accountServiceHomepage"
    const val OPENID = "${NAMESPACE}openid"
}
