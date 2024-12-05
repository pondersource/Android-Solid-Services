package com.pondersource.solidandroidapi.datamodule

import android.content.Context
import com.pondersource.shared.data.datamodule.DataModuleResult
import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.shared.data.datamodule.contact.CONTACTS_DIRECTORY_SUFFIX
import java.net.URI

class SolidContactsDataModuleImplementation: SolidContactsDataModule {

     companion object {
          @Volatile
          private lateinit var INSTANCE: SolidContactsDataModule

          fun getInstance(
               context: Context,
          ): SolidContactsDataModule {
               return if (Companion::INSTANCE.isInitialized) {
                    INSTANCE
               } else {
                    INSTANCE = SolidContactsDataModuleImplementation(context)
                    INSTANCE
               }
          }
     }

     val helper: SolidContactsDataModuleHelper

     private constructor(context: Context) {
          this.helper = SolidContactsDataModuleHelper.getInstance(context)
     }

     override suspend fun getAddressBooks(
          webId: String
     ): DataModuleResult<AddressBookList> {
          return try {
               val privates = helper.getPrivateAddressBooks(webId)
               val public = helper.getPublicAddressBooks(webId)
               DataModuleResult.Success(AddressBookList(
                    publicAddressBookUris = public,
                    privateAddressBookUris = privates
               ))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun createAddressBook(
          title: String,
          isPrivate: Boolean,
          storage: String,
          ownerWebId: String,
          container: String?
     ): DataModuleResult<AddressBook> {
          val newContainer = container ?: "${storage}${CONTACTS_DIRECTORY_SUFFIX}"
          return try {
               val createdAddressBookUri = helper.createAddressBook(
                    title,
                    newContainer,
                    isPrivate,
                    ownerWebId,
               ).getIdentifier().toString()
               getAddressBook(createdAddressBookUri)
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun getAddressBook(
          uri: String,
     ): DataModuleResult<AddressBook> {
          return try {
               val addressBookRdf = helper.getAddressBook(URI.create(uri))
               val addressBookContactsRdf = helper.getAddressBookContacts(URI.create(addressBookRdf.getNameEmailIndex()))
               val addressBookGroupsRdf = helper.getAddressBookGroups(URI.create(addressBookRdf.getGroupsIndex()))
               DataModuleResult.Success(AddressBook.createFromRdf(addressBookRdf, addressBookContactsRdf, addressBookGroupsRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun renameAddressBook(
          addressBookUri: String,
          newName: String
     ): DataModuleResult<AddressBook> {
          return try {
               helper.renameAddressBook(addressBookUri, newName)
               return getAddressBook(addressBookUri)
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun deleteAddressBook(addressBookUri: String, ownerWebId: String): DataModuleResult<AddressBook> {
          try {
               val addressBook = getAddressBook(addressBookUri)
               helper.deleteAddressBook(addressBookUri, ownerWebId)
               return addressBook
          } catch (e: Exception) {
               return DataModuleResult.Error(e.message)
          }
     }

     override suspend fun createNewContact(
          addressBookUri: String,
          newContact: NewContact,
          groupUris: List<String>
     ): DataModuleResult<FullContact> {
          return try {
               val newContact = helper.createContact(URI.create(addressBookUri), newContact)

               groupUris.forEach {
                    helper.addContactToGroup(newContact, URI.create(it))
               }
               return DataModuleResult.Success(FullContact.createFromRdf(newContact))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun getContact(
          contactUri: String
     ): DataModuleResult<FullContact> {
          return try {
               val contactRdf = helper.getContact(URI.create(contactUri))
               DataModuleResult.Success(FullContact.createFromRdf(contactRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun renameContact(
          contactUri: String,
          newName: String
     ): DataModuleResult<FullContact> {
          return try {
               val contactRdf = helper.renameContact(contactUri, newName)
               return DataModuleResult.Success(FullContact.createFromRdf(contactRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun addNewPhoneNumber(
          contactUri: String,
          newPhoneNumber: String
     ): DataModuleResult<FullContact> {
          return try {
               val contactRdf = helper.addNewPhoneNumber(URI.create(contactUri), newPhoneNumber)
               DataModuleResult.Success(FullContact.createFromRdf(contactRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun addNewEmailAddress(
          contactUri: String,
          newEmailAddress: String
     ): DataModuleResult<FullContact> {
          return try {
               val contactRdf = helper.addNewEmailAddress(URI.create(contactUri), newEmailAddress)
               DataModuleResult.Success(FullContact.createFromRdf(contactRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun removePhoneNumber(
          contactUri: String,
          phoneNumber: String
     ): DataModuleResult<FullContact> {
          return try {
               val contactRdf = helper.removePhoneNumberFromContact(URI.create(contactUri), phoneNumber)
               DataModuleResult.Success(FullContact.createFromRdf(contactRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun removeEmailAddress(
          contactUri: String,
          emailAddress: String
     ): DataModuleResult<FullContact> {
          return try {
               val contactRdf = helper.removeEmailAddressFromContact(URI.create(contactUri), emailAddress)
               DataModuleResult.Success(FullContact.createFromRdf(contactRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun deleteContact(
          addressBookUri: String,
          contactUri: String
     ): DataModuleResult<FullContact> {
          return try {
               val contact = getContact(contactUri)
               helper.deleteContact(addressBookUri, contactUri)
               contact
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun createNewGroup(
          addressBookUri: String,
          title: String,
          contactUris: List<String>,
     ): DataModuleResult<FullGroup> {
          return try {
               val groupRDF = helper.createGroup(URI.create(addressBookUri), title)

               contactUris.forEach {
                    helper.addContactToGroup(URI.create(it), groupRDF)
               }

               return getGroup(groupRDF.getIdentifier().toString())
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun getGroup(
          groupUri: String
     ): DataModuleResult<FullGroup> {
          return try {
               val groupRdf = helper.getGroup(URI.create(groupUri))
               DataModuleResult.Success(FullGroup.createFromRdf(groupRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun deleteGroup(
          addressBookUri: String,
          groupUri: String
     ): DataModuleResult<FullGroup> {
          return try {
               val groupRdf = helper.removeGroup(URI.create(addressBookUri), URI.create(groupUri))
               DataModuleResult.Success(FullGroup.createFromRdf(groupRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun addContactToGroup(
          contactUri: String,
          groupUri: String
     ): DataModuleResult<FullGroup> {
          return try {
               val groupRdf = helper.addContactToGroup(URI.create(contactUri), URI.create(groupUri))
               DataModuleResult.Success(FullGroup.createFromRdf(groupRdf))
          } catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }

     override suspend fun removeContactFromGroup(
          contactUri: String,
          groupUri: String
     ): DataModuleResult<FullGroup> {
          return try {
               val groupRdf = helper.removeContactFromGroup(contactUri, groupUri)
               DataModuleResult.Success(FullGroup.createFromRdf(groupRdf))
          }catch (e: Exception) {
               DataModuleResult.Error(e.message)
          }
     }
}