package com.erfangholami.androidsolidservices.api.datamodule.contacts

import android.content.Context
import com.erfangholami.androidsolidservices.shared.domain.datamodule.DataModuleResult
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBook
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBookList
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullContact
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullGroup
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.NewContact
import com.erfangholami.androidsolidservices.api.auth.Authenticator
import com.erfangholami.androidsolidservices.api.datamodule.contacts.implementation.SolidContactsDataModuleImplementation
import com.erfangholami.androidsolidservices.api.resource.SolidResourceManager

/**
 * Manages Solid Contacts data (address books, contacts, groups) on a user's Solid pod.
 *
 * All operations are performed on behalf of [ownerWebId] using the Solid Contacts
 * specification.  Results are wrapped in [DataModuleResult] to distinguish data errors
 * from unexpected exceptions.
 *
 * Obtain an instance via [SolidContactsDataModule.getInstance].
 */
public interface SolidContactsDataModule {

    public companion object {
        /**
         * Returns the application-scoped singleton [SolidContactsDataModule].
         * @param context Any [Context]; the application context is used internally.
         */
        public fun getInstance(authenticator: Authenticator): SolidContactsDataModule =
            SolidContactsDataModuleImplementation.getInstance(authenticator)

        public fun getInstance(resourceManager: SolidResourceManager): SolidContactsDataModule =
            SolidContactsDataModuleImplementation.getInstance(resourceManager)
    }

    //region AddressBooks
    public suspend fun getAddressBooks(
        ownerWebId: String
    ): DataModuleResult<AddressBookList>

    public suspend fun createAddressBook(
        ownerWebId: String,
        title: String,
        isPrivate: Boolean = true,
        storage: String,
        container: String? = null,
    ): DataModuleResult<AddressBook>

    public suspend fun getAddressBook(
        ownerWebId: String,
        addressBookUri: String,
    ): DataModuleResult<AddressBook>

    public suspend fun renameAddressBook(
        ownerWebId: String,
        addressBookUri: String,
        newName: String,
    ): DataModuleResult<AddressBook>

    public suspend fun deleteAddressBook(
        ownerWebId: String,
        addressBookUri: String,
    ): DataModuleResult<AddressBook>
    //endregion

    //region Contacts
    public suspend fun createNewContact(
        ownerWebId: String,
        addressBookString: String,
        newContact: NewContact,
        groupStrings: List<String> = emptyList(),
    ): DataModuleResult<FullContact>

    public suspend fun getContact(
        ownerWebId: String,
        contactString: String
    ): DataModuleResult<FullContact>

    public suspend fun renameContact(
        ownerWebId: String,
        contactString: String,
        newName: String,
    ): DataModuleResult<FullContact>

    public suspend fun addNewPhoneNumber(
        ownerWebId: String,
        contactString: String,
        newPhoneNumber: String,
    ): DataModuleResult<FullContact>

    public suspend fun addNewEmailAddress(
        ownerWebId: String,
        contactString: String,
        newEmailAddress: String,
    ): DataModuleResult<FullContact>

    public suspend fun removePhoneNumber(
        ownerWebId: String,
        contactString: String,
        phoneNumber: String,
    ): DataModuleResult<FullContact>

    public suspend fun removeEmailAddress(
        ownerWebId: String,
        contactString: String,
        emailAddress: String,
    ): DataModuleResult<FullContact>

    public suspend fun deleteContact(
        ownerWebId: String,
        addressBookUri: String,
        contactUri: String,
    ): DataModuleResult<FullContact>
    //endregion

    //region Groups
    public suspend fun createNewGroup(
        ownerWebId: String,
        addressBookString: String,
        title: String,
        contactUris: List<String> = emptyList(),
    ): DataModuleResult<FullGroup>

    public suspend fun getGroup(
        ownerWebId: String,
        groupString: String,
    ): DataModuleResult<FullGroup>

    public suspend fun deleteGroup(
        ownerWebId: String,
        addressBookString: String,
        groupString: String
    ): DataModuleResult<FullGroup>

    public suspend fun addContactToGroup(
        ownerWebId: String,
        contactString: String,
        groupString: String,
    ): DataModuleResult<FullGroup>

    public suspend fun removeContactFromGroup(
        ownerWebId: String,
        contactString: String,
        groupString: String,
    ): DataModuleResult<FullGroup>
    //endregion
}
