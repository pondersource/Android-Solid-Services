package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
import com.pondersource.shared.data.datamodule.contact.rdf.ContactRDF
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val uri: String,
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
    val uri: String,
    val fullName: String,
    val emailAddresses: List<Email>,
    val phoneNumbers: List<PhoneNumber>,
): Parcelable {
    companion object {
        fun createFromRdf(contactRdf: ContactRDF): FullContact {
            return FullContact(
                uri = contactRdf.getIdentifier().toString(),
                fullName = contactRdf.getFullName(),
                emailAddresses = contactRdf.getEmails(),
                phoneNumbers = contactRdf.getPhoneNumbers()
            )
        }
    }
}

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