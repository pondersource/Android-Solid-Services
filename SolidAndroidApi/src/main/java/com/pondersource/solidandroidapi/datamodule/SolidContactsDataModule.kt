package com.pondersource.solidandroidapi.datamodule

import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact

interface SolidContactsDataModule {


    suspend fun getAddressBooks(
        webId: String
    ): AddressBookList

    suspend fun createAddressBook(
        title: String,
        storage: String,
        ownerWebId: String,// TODO: we need default value
        container: String? = null,
    ) : String

    suspend fun getAddressBook(
        addressBookUri: String,
    ): AddressBook

    suspend fun renameAddressBook(
        addressBookUri: String,
        newName: String,
    )

    suspend fun createNewContact(
        addressBookString: String,
        newContact: NewContact,
        groupStrings: List<String> = emptyList(),
    ) : String

    suspend fun getContact(
        contactString: String
    ): FullContact

    suspend fun renameContact(
        contactString: String,
        newName: String,
    )

    suspend fun addNewPhoneNumber(
        contactString: String,
        newPhoneNumber: String,
    ): FullContact

    suspend fun addNewEmailAddress(
        contactString: String,
        newEmailAddress: String,
    ): FullContact

    suspend fun removePhoneNumber(
        contactString: String,
        phoneNumber: String,
    ): Boolean

    suspend fun removeEmailAddress(
        contactString: String,
        emailAddress: String,
    ): Boolean

    suspend fun createNewGroup(
        addressBookString: String,
        title: String,
    ): String

    suspend fun getGroup(
        groupString: String,
    ): FullGroup

    suspend fun removeGroup(
        addressBookString: String,
        groupString: String
    ): Boolean

    suspend fun addContactToGroup(
        contactString: String,
        groupString: String,
    )

    suspend fun removeContactFromGroup(
        contactString: String,
        groupString: String,
    ): Boolean
}