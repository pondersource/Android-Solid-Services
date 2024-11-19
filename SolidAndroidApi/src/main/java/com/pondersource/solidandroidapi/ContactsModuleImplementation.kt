package com.pondersource.solidandroidapi

import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import java.net.URI
import java.util.UUID

class ContactsModuleImplementation (
     override val webId: String,
     override val storage: String,
) : ContactsModule {

     override fun getAddressBooks(webId: String): AddressBookList {
          TODO("Not yet implemented")
     }

     override fun createAddressBook(
          title: String,
          container: String,
          ownerWebId: String
     ): URI {
          val id : String = UUID.randomUUID().toString()
          val uri : String = "${container}${id}/index#this"
          val nameEmailIndex : String = "${container}${id}/people"
          val groupIndex : String = "${container}${id}/groups"
          TODO("Not yet implemented")
     }

     override fun getAddressBook(
          uri: URI,
     ): AddressBook {
          TODO()
     }

     override fun createNewContact(
          addressBookUri: URI,
          newContact: NewContact,
          groupUris: List<URI>
     ): URI {
          TODO("Not yet implemented")
     }

     override fun getContact(contactUri: URI): FullContact {
          TODO("Not yet implemented")
     }

     override fun renameContact(contactUri: URI, newName: String) {
          TODO("Not yet implemented")
     }

     override fun createNewGroup(addressBookUri: URI, title: String): URI {
          TODO("Not yet implemented")
     }

     override fun getGroup(groupUri: URI): FullGroup {
          TODO("Not yet implemented")
     }

     override fun addContactToGroup(contactUri: URI, groupUri: URI) {
          TODO("Not yet implemented")
     }

     override fun removeContactFromGroup(contactUri: URI, groupUri: URI) {
          TODO("Not yet implemented")
     }

     override fun addNewPhoneNumber(
          contactUri: URI,
          newPhoneNumber: String
     ): URI {
          TODO("Not yet implemented")
     }

     override fun addNewEmailAddress(
          contactUri: URI,
          newEmailAddress: String
     ): URI {
          TODO("Not yet implemented")
     }

     override fun removePhoneNumber(contactUri: URI, phoneNumberUri: URI) {
          TODO("Not yet implemented")
     }

     override fun updatePhoneNumber(phoneNumberUri: URI, newPhoneNumber: String) {
          TODO("Not yet implemented")
     }

     override fun updateEmailAddress(
          emailAddressUri: URI,
          newEmailAddress: String
     ) {
          TODO("Not yet implemented")
     }

     override fun removeEmailAddress(contactUri: URI, emailAddressUri: URI) {
          TODO("Not yet implemented")
     }
}