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

     override suspend fun getAddressBooks(webId: String): AddressBookList {
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
     ): URI {
          val newContainer = container ?: "${storage}${CONTACTS_DIRECTORY_SUFFIX}"
          return helper.createAddressBook(title, newContainer, ownerWebId).getIdentifier()
     }

     override suspend fun getAddressBook(
          uri: URI,
     ): AddressBook {
          val addressBookRdf = helper.getAddressBook(uri)
          val addressBookContactsRdf = helper.getAddressBookContacts(URI.create(addressBookRdf.getNameEmailIndex()))
          val addressBookGroupsRdf = helper.getAddressBookGroups(URI.create(addressBookRdf.getGroupsIndex()))
          return AddressBook(
               uri = addressBookRdf.getIdentifier(),
               title = addressBookRdf.getTitle(),
               contacts = addressBookContactsRdf.getContacts(addressBookRdf.getIdentifier().toString()),
               groups = addressBookGroupsRdf.getGroups(addressBookRdf.getIdentifier().toString())
          )
     }

     override suspend fun createNewContact(
          addressBookUri: URI,
          newContact: NewContact,
          groupUris: List<URI>
     ): URI {
          val newContact = helper.createContact(addressBookUri, newContact)

          groupUris.forEach {
               helper.addContactToGroup(newContact, it)
          }
          return newContact.getIdentifier()
     }

     override suspend fun getContact(contactUri: URI): FullContact {
          val contactRdf = helper.getContact(contactUri)
          return FullContact(
               uri = contactRdf.getIdentifier(),
               fullName = contactRdf.getFullName(),
               emailAddresses = contactRdf.getEmails(),
               phoneNumbers = contactRdf.getPhoneNumbers()
          )
     }

     override suspend fun renameContact(contactUri: URI, newName: String) {
          TODO("Not yet implemented")
     }

     override suspend fun addNewPhoneNumber(
          contactUri: URI,
          newPhoneNumber: String
     ): FullContact {
          val contactRdf = helper.addNewPhoneNumber(contactUri, newPhoneNumber)
          return FullContact(
               uri = contactRdf.getIdentifier(),
               fullName = contactRdf.getFullName(),
               emailAddresses = contactRdf.getEmails(),
               phoneNumbers = contactRdf.getPhoneNumbers()
          )
     }

     override suspend fun addNewEmailAddress(
          contactUri: URI,
          newEmailAddress: String
     ): FullContact {
          val contactRdf = helper.addNewEmailAddress(contactUri, newEmailAddress)
          return FullContact(
               uri = contactRdf.getIdentifier(),
               fullName = contactRdf.getFullName(),
               emailAddresses = contactRdf.getEmails(),
               phoneNumbers = contactRdf.getPhoneNumbers()
          )
     }

     override suspend fun removePhoneNumber(contactUri: URI, phoneNumber: String): Boolean {
          return helper.removePhoneNumberFromContact(contactUri, phoneNumber)
     }

     override suspend fun removeEmailAddress(contactUri: URI, emailAddress: String): Boolean {
          return helper.removeEmailAddressFromContact(contactUri, emailAddress)
     }

     override suspend fun createNewGroup(addressBookUri: URI, title: String): URI {
          val groupRDF = helper.createGroup(addressBookUri, title)
          return groupRDF.getIdentifier()
     }

     override suspend fun getGroup(groupUri: URI): FullGroup {
          val groupRdf = helper.getGroup(groupUri)
          return FullGroup(
               uri = groupRdf.getIdentifier(),
               name = groupRdf.getTitle(),
               contacts = groupRdf.getContacts()
          )
     }

     override suspend fun removeGroup(addressBookUri: URI, groupUri: URI): Boolean {
          return helper.removeGroup(addressBookUri, groupUri)
     }

     override suspend fun addContactToGroup(contactUri: URI, groupUri: URI) {
          helper.addContactToGroup(contactUri, groupUri)
     }

     override suspend fun removeContactFromGroup(contactUri: URI, groupUri: URI): Boolean {
          return helper.removeContactFromGroup(contactUri, groupUri)
     }
}