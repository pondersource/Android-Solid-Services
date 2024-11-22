package com.pondersource.shared.data.datamodule.contact

import java.net.URI

data class Contact(
    val uri: URI,
    val name: String,
)

data class NewContact(
    var name: String,
    val email: String,
    val phoneNumber : String,
)

data class FullContact(
    val uri: URI,
    val fullName: String,
    val emailAddresses: List<Email>,
    val phoneNumbers: List<PhoneNumber>,
)

data class Email(
    val value: String,
)

data class PhoneNumber(
    val value: String,
)

enum class URLType {
    Home,
    Work,
    Homepage,
    WebId,
    PublicId,
}

data class Name(
    val familyName: String? = null,
    val givenName: String? = null,
    val additionalName: String? = null,
    val honorificPrefix: String? = null,
    val honorificSuffix: String? = null,
)