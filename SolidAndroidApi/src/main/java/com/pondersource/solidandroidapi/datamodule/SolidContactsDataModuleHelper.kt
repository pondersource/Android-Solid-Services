package com.pondersource.solidandroidapi.datamodule

import android.content.Context
import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.data.webid.PrivateTypeIndex
import com.pondersource.shared.data.webid.PublicTypeIndex
import com.pondersource.shared.data.webid.WebId
import com.pondersource.shared.data.webid.WebIdProfile
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.shared.data.datamodule.contact.rdf.AddressBookRDF
import com.pondersource.shared.data.datamodule.contact.rdf.ContactRDF
import com.pondersource.shared.data.datamodule.contact.rdf.GroupRDF
import com.pondersource.shared.data.datamodule.contact.rdf.GroupsIndexRDF
import com.pondersource.shared.data.datamodule.contact.rdf.NameEmailIndexRDF
import com.pondersource.shared.resource.SolidContainer
import com.pondersource.shared.data.datamodule.contact.GROUPS_FILE_NAME
import com.pondersource.shared.data.datamodule.contact.GROUP_DIRECTORY_SUFFIX
import com.pondersource.shared.data.datamodule.contact.INDEX_FILE_NAME
import com.pondersource.shared.data.datamodule.contact.PEOPLE_DIRECTORY_SUFFIX
import com.pondersource.shared.data.datamodule.contact.PEOPLE_FILE_NAME
import com.pondersource.solidandroidapi.SolidResourceManager
import com.pondersource.solidandroidapi.SolidResourceManagerImplementation
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModuleHelper.Companion
import java.net.URI
import java.util.UUID

class SolidContactsDataModuleHelper {

    companion object {
        private const val TAG = "SolidContactsDataModuleHelper"

        @Volatile
        private lateinit var INSTANCE: SolidContactsDataModuleHelper

        fun getInstance(
            context: Context,
        ): SolidContactsDataModuleHelper {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidContactsDataModuleHelper(context)
                INSTANCE
            }
        }
    }

    val solidResourceManager: SolidResourceManager

    private constructor(context: Context) {
        this.solidResourceManager = SolidResourceManagerImplementation.getInstance(context)
    }

    suspend fun createAddressBook(
        title: String,
        container: String,
        isPrivate: Boolean = true,
        ownerWebId: String
    ): AddressBookRDF {

        val id : String = UUID.randomUUID().toString()
        val uri : String = "${container}${id}/${INDEX_FILE_NAME}"
        val nameEmailIndex : String = "${container}${id}/${PEOPLE_FILE_NAME}"
        val groupIndex : String = "${container}${id}/${GROUPS_FILE_NAME}"

        val nemEmailIndex = NameEmailIndexRDF(
            identifier = URI.create(nameEmailIndex),
            mediaType = MediaType.JSON_LD,
            dataset = null,
            headers = null
        )

        val groupsIndexRDF = GroupsIndexRDF(
            identifier = URI.create(groupIndex),
            mediaType = MediaType.JSON_LD,
            dataset = null,
            headers = null
        )

        val addressBook = AddressBookRDF(
            identifier = URI.create(uri),
            mediaType = MediaType.JSON_LD,
            dataset = null,
            headers = null
        ).apply {
            setOwner(ownerWebId)
            setTitle(title)
            setNameEmailIndex(nameEmailIndex)
            setGroupsIndex(groupIndex)
        }

        solidResourceManager.create(nemEmailIndex).handleResponse()
        solidResourceManager.create(groupsIndexRDF).handleResponse()
        val newCreatedAddressBook = solidResourceManager.create(addressBook).handleResponse()
        updateTypeIndex(ownerWebId, newCreatedAddressBook.getIdentifier(), isPrivate)
        return newCreatedAddressBook
    }

    private suspend fun updateTypeIndex(ownerWebId: String, addressBookUri: URI, isPrivate: Boolean = true) {
        if (isPrivate) {
            val privateTypeIndex = getPrivateTypeIndex(ownerWebId)
            privateTypeIndex.addAddressBook(addressBookUri.toString())
            solidResourceManager.update(privateTypeIndex).handleResponse()
        } else {
            val publicTypeIndex = getPublicTypeIndex(ownerWebId)
            publicTypeIndex.addAddressBook(addressBookUri.toString())
            solidResourceManager.update(publicTypeIndex).handleResponse()
        }
    }

    suspend fun getAddressBook(uri: URI) : AddressBookRDF {
        return solidResourceManager.read(uri, AddressBookRDF::class.java).handleResponse()
    }

    suspend fun renameAddressBook(addressBookUri: String, newName: String): AddressBookRDF {
        val addressBookRdf = getAddressBook(URI.create(addressBookUri))
        if (addressBookRdf.getTitle() != newName) {
            addressBookRdf.setTitle(newName)
            solidResourceManager.update(addressBookRdf)

        }
        return addressBookRdf

    }

    suspend fun deleteAddressBook(addressBookUri: String, ownerWebId: String): AddressBookRDF {
        val addressBookRDF = getAddressBook(URI.create(addressBookUri))

        val privateTypeIndex = getPrivateTypeIndex(ownerWebId)
        val publicTypeIndex = getPublicTypeIndex(ownerWebId)
        if(privateTypeIndex.containsAddressBook(addressBookUri)) {
            privateTypeIndex.removeAddressBook(addressBookUri)
            solidResourceManager.update(privateTypeIndex).handleResponse()
        } else if (publicTypeIndex.containsAddressBook(addressBookUri)) {
            publicTypeIndex.removeAddressBook(addressBookUri)
            solidResourceManager.update(publicTypeIndex).handleResponse()
        }

        val addressBookMainContainer = addressBookUri.substring(0, addressBookUri.lastIndexOf("/") + 1)
        solidResourceManager.deleteContainer(URI.create(addressBookMainContainer))
        return addressBookRDF
    }

    suspend fun getAddressBookContacts(nameEmailIndexUri: URI): NameEmailIndexRDF {
        return solidResourceManager.read(nameEmailIndexUri, NameEmailIndexRDF::class.java).handleResponse()
    }

    suspend fun getAddressBookGroups(groupsIndexUri: URI): GroupsIndexRDF {
        return solidResourceManager.read(groupsIndexUri, GroupsIndexRDF::class.java).handleResponse()
    }

    suspend fun createContact(addressBookUri: URI, newContact: NewContact): ContactRDF {
        val contactId = UUID.randomUUID()
        val addressBookContainer = addressBookUri.toString().substring(0, addressBookUri.toString().lastIndexOf("/") + 1)
        val contactUri = "${addressBookContainer}${PEOPLE_DIRECTORY_SUFFIX}${contactId}/${INDEX_FILE_NAME}"
        val newContactRDF = createSingleContact(URI.create(contactUri), newContact)
        addContactToAddressBook(newContactRDF, addressBookUri)
        return newContactRDF
    }

    private suspend fun createSingleContact(contactUri: URI, newContact: NewContact): ContactRDF {
        val newContactRDF = ContactRDF(
            identifier = contactUri,
            mediaType = MediaType.JSON_LD,
            dataset = null,
            headers = null
        ).apply {
            setFullName(newContact.name)
            addPhoneNumber(newContact.phoneNumber)
            addEmailAddress(newContact.email)
        }
        return solidResourceManager.create(newContactRDF).handleResponse()
    }

    private suspend fun addContactToAddressBook(contact: ContactRDF, addressBookUri: URI) {
        val addressBookRDF = getAddressBook(addressBookUri)
        val nameEmailIndexRDF = getAddressBookContacts(URI.create(addressBookRDF.getNameEmailIndex()))
        nameEmailIndexRDF.addContact(addressBookRDF.getIdentifier().toString(), contact)
        solidResourceManager.update(nameEmailIndexRDF)
    }

    suspend fun addContactToGroup(contactUri: URI, groupUri: URI): GroupRDF {
        return addContactToGroup(getContact(contactUri), groupUri)
    }

    suspend fun addContactToGroup(contact: ContactRDF, groupUri: URI): GroupRDF {
        return addContactToGroup(contact, getGroup(groupUri))
    }

    suspend fun addContactToGroup(contactUri: URI, group: GroupRDF): GroupRDF {
        return addContactToGroup(getContact(contactUri), group)
    }

    suspend fun addContactToGroup(contact: ContactRDF, group: GroupRDF): GroupRDF {
        group.addMember(contact)
        solidResourceManager.update(group)
        return group
    }

    suspend fun getContact(contactUri: URI): ContactRDF {
        return solidResourceManager.read(contactUri, ContactRDF::class.java).handleResponse()
    }


    suspend fun deleteContact(addressBookUri: String, contactUri: String) {
        val addressBookRDF = getAddressBook(URI.create(addressBookUri))
        val nameEmailIndexRDF = getAddressBookContacts(URI.create(addressBookRDF.getNameEmailIndex()))
        if(nameEmailIndexRDF.removeContact(contactUri)) {
            solidResourceManager.update(nameEmailIndexRDF)
            val groupsIndexRDF = getAddressBookGroups(URI.create(addressBookRDF.getGroupsIndex()))
            groupsIndexRDF.getGroups(addressBookUri.toString()).forEach {
                removeContactFromGroup(contactUri, it.uri)
            }
        } else {
            //contact is not in this address book, so no need for search in groups
        }
        val contactDir = contactUri.substring(0, contactUri.lastIndexOf("/") + 1)
        solidResourceManager.deleteContainer(URI.create(contactDir))

    }

    suspend fun createGroup(addressBookUri: URI, groupName: String): GroupRDF {
        val addressBookContainer = addressBookUri.toString().substring(0, addressBookUri.toString().lastIndexOf("/") + 1)
        val groupUri = "${addressBookContainer}${GROUP_DIRECTORY_SUFFIX}${groupName.trim().replace(" ", "_")}.ttl"
        val group = createSingleGroup(addressBookUri, URI.create(groupUri), groupName)
        addGroupToAddressBook(addressBookUri, group)
        return group
    }

    private suspend fun createSingleGroup(
        addressBookUri: URI,
        groupUri: URI,
        groupName: String
    ): GroupRDF {
        val groupRdf = GroupRDF(
            identifier = groupUri,
            mediaType = MediaType.JSON_LD,
            dataset = null,
            headers = null
        ).apply {
            setTitle(groupName)
            setIncludesInAddressBook(addressBookUri.toString())
        }
        return solidResourceManager.create(groupRdf).handleResponse()
    }

    private suspend fun addGroupToAddressBook(addressBookUri: URI, group: GroupRDF) {
        val addressBookRDF = getAddressBook(addressBookUri)
        val groupsIndexRDF = getAddressBookGroups(URI.create(addressBookRDF.getGroupsIndex()))
        groupsIndexRDF.addGroup(addressBookRDF.getIdentifier().toString(), group)
        solidResourceManager.update(groupsIndexRDF)
    }

    suspend fun getGroup(groupUri: URI): GroupRDF {
        return solidResourceManager.read(groupUri, GroupRDF::class.java).handleResponse()
    }

    suspend fun removeContactFromGroup(contactUri: String, groupUri: String): GroupRDF {
        val groupRdf = getGroup(URI.create(groupUri))
        val result = groupRdf.removeMember(URI.create(contactUri))
        if (result) {
            solidResourceManager.update(groupRdf).handleResponse()
        }
        return groupRdf
    }

    suspend fun addNewPhoneNumber(contactUri: URI, newPhoneNumber: String) : ContactRDF {
        val contact = getContact(contactUri)
        val addingResult = contact.addPhoneNumber(newPhoneNumber)
        return if (addingResult) {
            solidResourceManager.update(contact).handleResponse()
        } else {
            contact
        }
    }

    suspend fun addNewEmailAddress(contactUri: URI, newEmailAddress: String): ContactRDF {
        val contact = getContact(contactUri)
        val addingResult = contact.addEmailAddress(newEmailAddress)
        return if (addingResult) {
            solidResourceManager.update(contact).handleResponse()
        } else {
            contact
        }
    }

    suspend fun removePhoneNumberFromContact(contactUri: URI, phoneNumber: String): ContactRDF {
        val contactRdf = getContact(contactUri)
        if(contactRdf.removePhoneNumber(phoneNumber)) {
            solidResourceManager.update(contactRdf).handleResponse()
        }
        return contactRdf
    }

    suspend fun removeEmailAddressFromContact(contactUri: URI, emailAddress: String): ContactRDF {
        val contactRdf = getContact(contactUri)
        if(contactRdf.removeEmailAddress(emailAddress)) {
            solidResourceManager.update(contactRdf).handleResponse()
        }
        return contactRdf
    }

    suspend fun removeGroup(addressBookUri: URI, groupUri: URI): GroupRDF {
        val addressBookRDF = getAddressBook(addressBookUri)
        val groupRdf = getGroup(groupUri)
        val groupsIndexRDF = getAddressBookGroups(URI.create(addressBookRDF.getGroupsIndex()))
        if (groupsIndexRDF.removeGroup(groupUri)) {
            solidResourceManager.update(groupsIndexRDF).handleResponse()
            solidResourceManager.delete(groupRdf).handleResponse()
        }
        return groupRdf
    }

    suspend fun getPrivateAddressBooks(webId: String): List<String> {
        return getPrivateTypeIndex(webId).getAddressBooks()
    }

    suspend fun getPublicAddressBooks(webId: String): List<String> {
        return getPublicTypeIndex(webId).getAddressBooks()
    }

    suspend fun renameContact(contactUri: String, newName: String): ContactRDF {
        val contactRdf = getContact(URI.create(contactUri))
        if(contactRdf.getFullName() != newName) {
            contactRdf.setFullName(newName)
            solidResourceManager.update(contactRdf)
        }
        return contactRdf
    }


    private suspend fun getPrivateTypeIndex(webIdString: String): PrivateTypeIndex {
        val webId = solidResourceManager.read(URI.create(webIdString), WebId::class.java).handleResponse()
        var privateTypeIndexUri = webId.getPrivateTypeIndex()

        if (privateTypeIndexUri == null) {
            val webIdProfile = solidResourceManager.read(URI.create(webId.getProfileUrl()),
                WebIdProfile::class.java).handleResponse()
            privateTypeIndexUri = webIdProfile.getPrivateTypeIndex()

            if (privateTypeIndexUri == null) {
                webIdProfile.setPrivateTypeIndex(webIdString, webId.getStorages()[0].toString())
                solidResourceManager.update(webIdProfile).handleResponse()
                privateTypeIndexUri = webIdProfile.getPrivateTypeIndex()
                solidResourceManager.create(
                    PrivateTypeIndex(
                        URI.create(privateTypeIndexUri),
                        MediaType.JSON_LD,
                        null,
                        null
                    )
                ).handleResponse()
            }
        }

        return solidResourceManager.read(
            URI.create(privateTypeIndexUri),
            PrivateTypeIndex::class.java
        ).handleResponse()
    }

    private suspend fun getPublicTypeIndex(webIdString: String): PublicTypeIndex {
        val webId = solidResourceManager.read(URI.create(webIdString), WebId::class.java).handleResponse()
        var publicTypeIndexUri = webId.getPublicTypeIndex()

        if (publicTypeIndexUri == null) {
            val webIdProfile = solidResourceManager.read(URI.create(webId.getProfileUrl()),
                WebIdProfile::class.java).handleResponse()
            publicTypeIndexUri = webIdProfile.getPublicTypeIndex()

            if (publicTypeIndexUri == null) {
                webIdProfile.setPublicTypeIndex(webIdString, webId.getStorages()[0].toString())
                solidResourceManager.update(webIdProfile).handleResponse()
                publicTypeIndexUri = webIdProfile.getPublicTypeIndex()
                solidResourceManager.create(
                    PublicTypeIndex(
                        URI.create(publicTypeIndexUri),
                        MediaType.JSON_LD,
                        null,
                        null
                    )
                ).handleResponse()
            }
        }

        return solidResourceManager.read(
            URI.create(publicTypeIndexUri),
            PublicTypeIndex::class.java
        ).handleResponse()
    }
}