package com.pondersource.solidandroidapi.datamodule

import com.pondersource.shared.data.datamodule.DataModuleResult
import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact

interface SolidContactsDataModule {

    suspend fun getAddressBooks(
        webId: String
    ): DataModuleResult<AddressBookList>

    suspend fun createAddressBook(
        title: String,
        isPrivate: Boolean = true,
        storage: String,
        ownerWebId: String,// TODO: we need default value
        container: String? = null,
    ) : DataModuleResult<AddressBook>

    suspend fun getAddressBook(
        addressBookUri: String,
    ): DataModuleResult<AddressBook>

    suspend fun renameAddressBook(
        addressBookUri: String,
        newName: String,
    ): DataModuleResult<AddressBook>

    suspend fun createNewContact(
        addressBookString: String,
        newContact: NewContact,
        groupStrings: List<String> = emptyList(),
    ) : DataModuleResult<FullContact>

    suspend fun getContact(
        contactString: String
    ): DataModuleResult<FullContact>

    suspend fun renameContact(
        contactString: String,
        newName: String,
    ): DataModuleResult<FullContact>

    suspend fun addNewPhoneNumber(
        contactString: String,
        newPhoneNumber: String,
    ): DataModuleResult<FullContact>

    suspend fun addNewEmailAddress(
        contactString: String,
        newEmailAddress: String,
    ): DataModuleResult<FullContact>

    suspend fun removePhoneNumber(
        contactString: String,
        phoneNumber: String,
    ): DataModuleResult<FullContact>

    suspend fun removeEmailAddress(
        contactString: String,
        emailAddress: String,
    ): DataModuleResult<FullContact>

    suspend fun createNewGroup(
        addressBookString: String,
        title: String,
    ): DataModuleResult<FullGroup>

    suspend fun getGroup(
        groupString: String,
    ): DataModuleResult<FullGroup>

    suspend fun removeGroup(
        addressBookString: String,
        groupString: String
    ): DataModuleResult<FullGroup>

    suspend fun addContactToGroup(
        contactString: String,
        groupString: String,
    ): DataModuleResult<FullGroup>

    suspend fun removeContactFromGroup(
        contactString: String,
        groupString: String,
    ): DataModuleResult<FullGroup>
}