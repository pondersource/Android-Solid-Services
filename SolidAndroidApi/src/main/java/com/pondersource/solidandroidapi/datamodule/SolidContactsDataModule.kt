package com.pondersource.solidandroidapi.datamodule

import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import java.net.URI

interface SolidContactsDataModule {


    suspend fun getAddressBooks(webId: String): AddressBookList

    suspend fun createAddressBook(
        title: String,
        storage: String,
        ownerWebId: String,// TODO: we need default value
        container: String? = null,
    ) : URI

    suspend fun getAddressBook(
        uri: URI,
    ): AddressBook

    suspend fun createNewContact(
        addressBookUri: URI,
        newContact: NewContact,
        groupUris: List<URI> = emptyList(),
    ) : URI

    suspend fun getContact(
        contactUri: URI
): FullContact

    suspend fun renameContact(
        contactUri: URI,
        newName: String,
    )

    suspend fun addNewPhoneNumber(
        contactUri: URI,
        newPhoneNumber: String,
    ): FullContact

    suspend fun addNewEmailAddress(
        contactUri: URI,
        newEmailAddress: String,
    ): FullContact

    suspend fun removePhoneNumber(
        contactUri: URI,
        phoneNumber: String,
    ): Boolean

    suspend fun removeEmailAddress(
        contactUri: URI,
        emailAddress: String,
    ): Boolean

    suspend fun createNewGroup(
        addressBookUri: URI,
        title: String,
    ): URI

    suspend fun getGroup(
        groupUri: URI,
    ): FullGroup

    suspend fun removeGroup(
        addressBookUri: URI,
        groupUri: URI
    ): Boolean

    suspend fun addContactToGroup(
        contactUri: URI,
        groupUri: URI,
    )

    suspend fun removeContactFromGroup(
        contactUri: URI,
        groupUri: URI,
    ): Boolean
}