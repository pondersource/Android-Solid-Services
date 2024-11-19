package com.pondersource.solidandroidapi

import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import java.net.URI

interface ContactsModule {

    val webId: String
    val storage: String

    fun getAddressBooks(webId: String = this.webId): AddressBookList

    fun createAddressBook(
        title: String,
        container: String = "${storage}${CONTACTS_DIRECTORY_SUFFIX}",
        ownerWebId: String, // TODO: we need default value
    ) : URI

    fun getAddressBook(
        uri: URI,
    ): AddressBook

    fun createNewContact(
        addressBookUri: URI,
        newContact: NewContact,
        groupUris: List<URI> = emptyList(),
    ) : URI

    fun getContact(
        contactUri: URI
    ): FullContact

    fun renameContact(
        contactUri: URI,
        newName: String,
    )

    fun createNewGroup(
        addressBookUri: URI,
        title: String,
    ): URI

    fun getGroup(
        groupUri: URI,
    ): FullGroup

    fun addContactToGroup(
        contactUri: URI,
        groupUri: URI,
    )

    fun removeContactFromGroup(
        contactUri: URI,
        groupUri: URI,
    )

    fun addNewPhoneNumber(
        contactUri: URI,
        newPhoneNumber: String,
    ): URI

    fun addNewEmailAddress(
        contactUri: URI,
        newEmailAddress: String,
    ): URI

    fun removePhoneNumber(
        contactUri: URI,
        phoneNumberUri: URI,
    )

    fun updatePhoneNumber(
        phoneNumberUri: URI,
        newPhoneNumber: String,
    )

    fun updateEmailAddress(
        emailAddressUri: URI,
        newEmailAddress: String,
    )

    fun removeEmailAddress(
        contactUri: URI,
        emailAddressUri: URI,
    )
}