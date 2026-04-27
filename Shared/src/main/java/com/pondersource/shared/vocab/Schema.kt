package com.pondersource.shared.vocab

/**
 * Schema.org vocabulary constants.
 * https://schema.org/
 * Broadly used in Solid apps for typed data (media, events, products, etc.).
 */
object Schema {
    const val NAMESPACE = "https://schema.org/"

    // Types
    const val THING = "${NAMESPACE}Thing"
    const val CREATIVE_WORK = "${NAMESPACE}CreativeWork"
    const val MEDIA_OBJECT = "${NAMESPACE}MediaObject"
    const val MOVIE = "${NAMESPACE}Movie"
    const val MUSIC_RECORDING = "${NAMESPACE}MusicRecording"
    const val BOOK = "${NAMESPACE}Book"
    const val ARTICLE = "${NAMESPACE}Article"
    const val BLOG_POSTING = "${NAMESPACE}BlogPosting"
    const val EVENT = "${NAMESPACE}Event"
    const val PERSON = "${NAMESPACE}Person"
    const val ORGANIZATION = "${NAMESPACE}Organization"
    const val PLACE = "${NAMESPACE}Place"
    const val PRODUCT = "${NAMESPACE}Product"
    const val OFFER = "${NAMESPACE}Offer"

    // Descriptive predicates
    const val NAME = "${NAMESPACE}name"
    const val DESCRIPTION = "${NAMESPACE}description"
    const val IMAGE = "${NAMESPACE}image"
    const val URL = "${NAMESPACE}url"
    const val IDENTIFIER = "${NAMESPACE}identifier"
    const val SAME_AS = "${NAMESPACE}sameAs"
    const val ADDITIONAL_TYPE = "${NAMESPACE}additionalType"

    // Creative work predicates
    const val DATE_CREATED = "${NAMESPACE}dateCreated"
    const val DATE_MODIFIED = "${NAMESPACE}dateModified"
    const val DATE_PUBLISHED = "${NAMESPACE}datePublished"
    const val AUTHOR = "${NAMESPACE}author"
    const val CREATOR = "${NAMESPACE}creator"
    const val PUBLISHER = "${NAMESPACE}publisher"
    const val GENRE = "${NAMESPACE}genre"
    const val KEYWORDS = "${NAMESPACE}keywords"
    const val CONTENT_URL = "${NAMESPACE}contentUrl"
    const val ENCODING_FORMAT = "${NAMESPACE}encodingFormat"
    const val DURATION = "${NAMESPACE}duration"
    const val IN_LANGUAGE = "${NAMESPACE}inLanguage"
    const val THUMBNAIL_URL = "${NAMESPACE}thumbnailUrl"
    const val LICENSE = "${NAMESPACE}license"

    // Person / Organization predicates
    const val GIVEN_NAME = "${NAMESPACE}givenName"
    const val FAMILY_NAME = "${NAMESPACE}familyName"
    const val EMAIL = "${NAMESPACE}email"
    const val TELEPHONE = "${NAMESPACE}telephone"
    const val ADDRESS = "${NAMESPACE}address"
    const val MEMBER_OF = "${NAMESPACE}memberOf"
    const val KNOWS = "${NAMESPACE}knows"
    const val BIRTH_DATE = "${NAMESPACE}birthDate"

    // Event predicates
    const val START_DATE = "${NAMESPACE}startDate"
    const val END_DATE = "${NAMESPACE}endDate"
    const val LOCATION = "${NAMESPACE}location"
    const val ORGANIZER = "${NAMESPACE}organizer"
    const val ATTENDEE = "${NAMESPACE}attendee"
}
