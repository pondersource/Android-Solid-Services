package com.pondersource.solidandroidapi.datamodule

import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.solidandroidapi.CONTACTS_DIRECTORY_SUFFIX
import java.net.URI

class SolidContactsDataModuleImplementation (
     val helper: SolidContactsDataModuleHelper,
) : SolidContactsDataModule {

     override suspend fun getAddressBooks(
          webId: String
     ): AddressBookList {
          val privates = helper.getPrivateAddressBooks(webId)
          val public = helper.getPublicAddressBooks(webId)
          return AddressBookList(
               publicAddressBookUris = public,
               privateAddressBookUris = privates
          )
     }

     override suspend fun createAddressBook(
          title: String,
          storage: String,
          ownerWebId: String,
          container: String?,
     ): String {
          val newContainer = container ?: "${storage}${CONTACTS_DIRECTORY_SUFFIX}"
          return helper.createAddressBook(title, newContainer, ownerWebId).getIdentifier().toString()
     }

     override suspend fun getAddressBook(
          uri: String,
     ): AddressBook {
          val addressBookRdf = helper.getAddressBook(URI.create(uri))
          val addressBookContactsRdf = helper.getAddressBookContacts(URI.create(addressBookRdf.getNameEmailIndex()))
          val addressBookGroupsRdf = helper.getAddressBookGroups(URI.create(addressBookRdf.getGroupsIndex()))
          return AddressBook(
               uri = addressBookRdf.getIdentifier().toString(),
               title = addressBookRdf.getTitle(),
               contacts = addressBookContactsRdf.getContacts(addressBookRdf.getIdentifier().toString()),
               groups = addressBookGroupsRdf.getGroups(addressBookRdf.getIdentifier().toString())
          )
     }

     override suspend fun renameAddressBook(addressBookUri: String, newName: String) {
          helper.renameAddressBook(addressBookUri, newName)
     }

     override suspend fun createNewContact(
          addressBookUri: String,
          newContact: NewContact,
          groupUris: List<String>
     ): String {
          val newContact = helper.createContact(URI.create(addressBookUri), newContact)

          groupUris.forEach {
               helper.addContactToGroup(newContact, URI.create(it))
          }
          return newContact.getIdentifier().toString()
     }

     override suspend fun getContact(contactUri: String): FullContact {
          val contactRdf = helper.getContact(URI.create(contactUri))
          return FullContact(
               uri = contactRdf.getIdentifier().toString(),
               fullName = contactRdf.getFullName(),
               emailAddresses = contactRdf.getEmails(),
               phoneNumbers = contactRdf.getPhoneNumbers()
          )
     }

     override suspend fun renameContact(contactUri: String, newName: String) {
          helper.renameContact(contactUri, newName)
     }

     override suspend fun addNewPhoneNumber(
          contactUri: String,
          newPhoneNumber: String
     ): FullContact {
          val contactRdf = helper.addNewPhoneNumber(URI.create(contactUri), newPhoneNumber)
          return FullContact(
               uri = contactRdf.getIdentifier().toString(),
               fullName = contactRdf.getFullName(),
               emailAddresses = contactRdf.getEmails(),
               phoneNumbers = contactRdf.getPhoneNumbers()
          )
     }

     override suspend fun addNewEmailAddress(
          contactUri: String,
          newEmailAddress: String
     ): FullContact {
          val contactRdf = helper.addNewEmailAddress(URI.create(contactUri), newEmailAddress)
          return FullContact(
               uri = contactRdf.getIdentifier().toString(),
               fullName = contactRdf.getFullName(),
               emailAddresses = contactRdf.getEmails(),
               phoneNumbers = contactRdf.getPhoneNumbers()
          )
     }

     override suspend fun removePhoneNumber(contactUri: String, phoneNumber: String): Boolean {
          return helper.removePhoneNumberFromContact(URI.create(contactUri), phoneNumber)
     }

     override suspend fun removeEmailAddress(contactUri: String, emailAddress: String): Boolean {
          return helper.removeEmailAddressFromContact(URI.create(contactUri), emailAddress)
     }

     override suspend fun createNewGroup(addressBookUri: String, title: String): String {
          val groupRDF = helper.createGroup(URI.create(addressBookUri), title)
          return groupRDF.getIdentifier().toString()
     }

     override suspend fun getGroup(groupUri: String): FullGroup {
          val groupRdf = helper.getGroup(URI.create(groupUri))
          return FullGroup(
               uri = groupRdf.getIdentifier().toString(),
               name = groupRdf.getTitle(),
               contacts = groupRdf.getContacts()
          )
     }

     override suspend fun removeGroup(addressBookUri: String, groupUri: String): Boolean {
          return helper.removeGroup(URI.create(addressBookUri), URI.create(groupUri))
     }

     override suspend fun addContactToGroup(contactUri: String, groupUri: String) {
          helper.addContactToGroup(URI.create(contactUri), URI.create(groupUri))
     }

     override suspend fun removeContactFromGroup(contactUri: String, groupUri: String): Boolean {
          return helper.removeContactFromGroup(URI.create(contactUri), URI.create(groupUri))
     }
}