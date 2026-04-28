package com.pondersource.solidandroidapi.datamodule

import android.content.Context
import com.pondersource.shared.domain.datamodule.DataModuleResult
import com.pondersource.shared.domain.datamodule.contact.AddressBook
import com.pondersource.shared.domain.datamodule.contact.AddressBookList
import com.pondersource.shared.domain.datamodule.contact.FullContact
import com.pondersource.shared.domain.datamodule.contact.FullGroup
import com.pondersource.shared.domain.datamodule.contact.NewContact

/**
 * Manages Solid Contacts data (address books, contacts, groups) on a user's Solid pod.
 *
 * All operations are performed on behalf of [ownerWebId] using the Solid Contacts
 * specification.  Results are wrapped in [DataModuleResult] to distinguish data errors
 * from unexpected exceptions.
 *
 * Obtain an instance via [SolidContactsDataModule.getInstance].
 */
interface SolidContactsDataModule {

    companion object {
        fun getInstance(context: Context): SolidContactsDataModule = SolidContactsDataModuleImplementation.getInstance(context)
    }

    //region AddressBooks
    suspend fun getAddressBooks(
        ownerWebId: String
    ): DataModuleResult<AddressBookList>

    suspend fun createAddressBook(
        ownerWebId: String,
        title: String,
        isPrivate: Boolean = true,
        storage: String,
        container: String? = null,
    ) : DataModuleResult<AddressBook>

    suspend fun getAddressBook(
        ownerWebId: String,
        addressBookUri: String,
    ): DataModuleResult<AddressBook>

    suspend fun renameAddressBook(
        ownerWebId: String,
        addressBookUri: String,
        newName: String,
    ): DataModuleResult<AddressBook>

    suspend fun deleteAddressBook(
        ownerWebId: String,
        addressBookUri: String,
    ): DataModuleResult<AddressBook>
    //endregion

    //region Contacts
    suspend fun createNewContact(
        ownerWebId: String,
        addressBookString: String,
        newContact: NewContact,
        groupStrings: List<String> = emptyList(),
    ) : DataModuleResult<FullContact>

    suspend fun getContact(
        ownerWebId: String,
        contactString: String
    ): DataModuleResult<FullContact>

    suspend fun renameContact(
        ownerWebId: String,
        contactString: String,
        newName: String,
    ): DataModuleResult<FullContact>

    suspend fun addNewPhoneNumber(
        ownerWebId: String,
        contactString: String,
        newPhoneNumber: String,
    ): DataModuleResult<FullContact>

    suspend fun addNewEmailAddress(
        ownerWebId: String,
        contactString: String,
        newEmailAddress: String,
    ): DataModuleResult<FullContact>

    suspend fun removePhoneNumber(
        ownerWebId: String,
        contactString: String,
        phoneNumber: String,
    ): DataModuleResult<FullContact>

    suspend fun removeEmailAddress(
        ownerWebId: String,
        contactString: String,
        emailAddress: String,
    ): DataModuleResult<FullContact>

    suspend fun deleteContact(
        ownerWebId: String,
        addressBookUri: String,
        contactUri: String,
    ): DataModuleResult<FullContact>
    //endregion

    //region Groups
    suspend fun createNewGroup(
        ownerWebId: String,
        addressBookString: String,
        title: String,
        contactUris: List<String> = emptyList(),
    ): DataModuleResult<FullGroup>

    suspend fun getGroup(
        ownerWebId: String,
        groupString: String,
    ): DataModuleResult<FullGroup>

    suspend fun deleteGroup(
        ownerWebId: String,
        addressBookString: String,
        groupString: String
    ): DataModuleResult<FullGroup>

    suspend fun addContactToGroup(
        ownerWebId: String,
        contactString: String,
        groupString: String,
    ): DataModuleResult<FullGroup>

    suspend fun removeContactFromGroup(
        ownerWebId: String,
        contactString: String,
        groupString: String,
    ): DataModuleResult<FullGroup>
    //endregion
}