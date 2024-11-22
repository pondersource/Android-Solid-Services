package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URI

@Parcelize
data class Contact(
    val uri: URI,
    val name: String,
): Parcelable

@Parcelize
data class NewContact(
    var name: String,
    val email: String,
    val phoneNumber : String,
): Parcelable

@Parcelize
data class FullContact(
    val uri: URI,
    val fullName: String,
    val emailAddresses: List<Email>,
    val phoneNumbers: List<PhoneNumber>,
): Parcelable

@Parcelize
data class Email(
    val value: String,
): Parcelable

@Parcelize
data class PhoneNumber(
    val value: String,
): Parcelable

enum class URLType {
    Home,
    Work,
    Homepage,
    WebId,
    PublicId,
}

@Parcelize
data class Name(
    val familyName: String? = null,
    val givenName: String? = null,
    val additionalName: String? = null,
    val honorificPrefix: String? = null,
    val honorificSuffix: String? = null,
): Parcelable