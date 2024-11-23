package com.pondersource.solidandroidapi.datamodule

import com.apicatalog.jsonld.http.media.MediaType
import com.pondersource.shared.data.WebId
import com.pondersource.shared.data.WebIdProfile
import com.pondersource.shared.data.SettingTypeIndex
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.shared.data.datamodule.contact.rdf.AddressBookRDF
import com.pondersource.shared.data.datamodule.contact.rdf.ContactRDF
import com.pondersource.shared.data.datamodule.contact.rdf.GroupRDF
import com.pondersource.shared.data.datamodule.contact.rdf.GroupsIndexRDF
import com.pondersource.shared.data.datamodule.contact.rdf.NameEmailIndexRDF
import com.pondersource.shared.resource.SolidContainer
import com.pondersource.solidandroidapi.GROUPS_FILE_NAME
import com.pondersource.solidandroidapi.GROUP_DIRECTORY_SUFFIX
import com.pondersource.solidandroidapi.INDEX_FILE_NAME
import com.pondersource.solidandroidapi.PEOPLE_DIRECTORY_SUFFIX
import com.pondersource.solidandroidapi.PEOPLE_FILE_NAME
import com.pondersource.solidandroidapi.SolidResourceManager
import java.net.URI
import java.util.UUID

class SolidContactsDataModuleHelper(
    val solidResourceManager: SolidResourceManager,
) {

    companion object {
        private const val TAG = "SolidContactsDataModuleHelper"
    }

    suspend fun createAddressBook(
        title: String,
        container: String,
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

        solidResourceManager.create(nemEmailIndex)
        solidResourceManager.create(groupsIndexRDF)
        val newCreatedAddressBook = solidResourceManager.create(addressBook).handleResponse()
        //TODO(Updates the privateTypeIndex but it won't be ttl(saves as JSON_LD))
        //updateTypeIndex(ownerWebId, newCreatedAddressBook.getIdentifier())
        return newCreatedAddressBook
    }

    private suspend fun updateTypeIndex(ownerWebId: String, addressBookUri: URI) {
        val privateTypeIndex = getPrivateTypeIndex(ownerWebId)
        privateTypeIndex.addAddressBook(addressBookUri.toString())
        solidResourceManager.update(privateTypeIndex)
    }

    suspend fun getAddressBook(uri: URI) : AddressBookRDF {
        return solidResourceManager.read(uri, AddressBookRDF::class.java).handleResponse()
    }

    suspend fun renameAddressBook(addressBookUri: String, newName: String) {
        val addressBookRdf = getAddressBook(URI.create(addressBookUri))
        if (addressBookRdf.getTitle() != newName) {
            addressBookRdf.setTitle(newName)
            solidResourceManager.update(addressBookRdf)
        }

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

    suspend fun addContactToGroup(contactUri: URI, groupUri: URI) {
        addContactToGroup(getContact(contactUri), groupUri)
    }

    suspend fun addContactToGroup(contact: ContactRDF, groupUri: URI) {
        val groupRdf = getGroup(groupUri)
        groupRdf.addMember(contact)
        solidResourceManager.update(groupRdf)
    }

    suspend fun getContact(contactUri: URI): ContactRDF {
        return solidResourceManager.read(contactUri, ContactRDF::class.java).handleResponse()
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

    suspend fun removeContactFromGroup(contactUri: URI, groupUri: URI): Boolean {
        val groupRdf = getGroup(groupUri)
        val result = groupRdf.removeMember(contactUri)
        if (result) {
            solidResourceManager.update(groupRdf).handleResponse()
            return true
        }
        return false
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

    suspend fun removePhoneNumberFromContact(contactUri: URI, phoneNumber: String): Boolean {
        val contactRdf = getContact(contactUri)
        return if(contactRdf.removePhoneNumber(phoneNumber)) {
            solidResourceManager.update(contactRdf).handleResponse()
            true
        } else {
            false
        }
    }

    suspend fun removeEmailAddressFromContact(contactUri: URI, emailAddress: String): Boolean {
        val contactRdf = getContact(contactUri)
        return if(contactRdf.removeEmailAddress(emailAddress)) {
            solidResourceManager.update(contactRdf).handleResponse()
            true
        } else {
            false
        }
    }

    suspend fun removeGroup(addressBookUri: URI, groupUri: URI): Boolean {
        val addressBookRDF = getAddressBook(addressBookUri)
        val groupRdf = getGroup(groupUri)
        val groupsIndexRDF = getAddressBookGroups(URI.create(addressBookRDF.getGroupsIndex()))
        if (groupsIndexRDF.removeGroup(groupUri)) {
            solidResourceManager.update(groupsIndexRDF).handleResponse()
            solidResourceManager.delete(groupRdf).handleResponse()
            return true
        } else {
            return false
        }
    }

    suspend fun removeContactFromAddressBook(addressBookUri: URI, contactUri: URI) {
        val addressBookRDF = getAddressBook(addressBookUri)
        val nameEmailIndexRDF = getAddressBookContacts(URI.create(addressBookRDF.getNameEmailIndex()))
        if(nameEmailIndexRDF.removeContact(addressBookUri.toString(), contactUri.toString())) {
            solidResourceManager.update(nameEmailIndexRDF)
            val groupsIndexRDF = getAddressBookGroups(URI.create(addressBookRDF.getGroupsIndex()))
            groupsIndexRDF.getGroups(addressBookUri.toString()).forEach {
                removeContactFromGroup(URI.create(it.uri), contactUri)
            }
        } else {
            //contact is not in this address book, so no need for search in groups
        }
        val contactDir = contactUri.toString().substring(0, contactUri.toString().lastIndexOf("${PEOPLE_DIRECTORY_SUFFIX}/") + 1)
        val container = SolidContainer(
            identifier = URI.create(contactDir),
        )
        solidResourceManager.delete(container)
    }

    suspend fun getPrivateAddressBooks(webId: String): List<String> {
        return getPrivateTypeIndex(webId).getAddressBooks()
    }

    suspend fun getPublicAddressBooks(webId: String): List<String> {
        return getPublicTypeIndex(webId).getAddressBooks()
    }

    private suspend fun getPrivateTypeIndex(webId: String): SettingTypeIndex {
        val webIdProfile = solidResourceManager.read(URI.create(webId), WebId::class.java).handleResponse()
        var privateTypeIndexUri = webIdProfile.getPrivateTypeIndex()

        if (privateTypeIndexUri == null) {
            val webIdProfile = solidResourceManager.read(URI.create(webIdProfile.getProfileUrl()),
                WebIdProfile::class.java).handleResponse()
            privateTypeIndexUri = webIdProfile.getPrivateTypeIndex()
        }

        return solidResourceManager.read(URI.create(privateTypeIndexUri), SettingTypeIndex::class.java).handleResponse()
    }

    private suspend fun getPublicTypeIndex(webId: String): SettingTypeIndex {
        val webIdProfile = solidResourceManager.read(URI.create(webId), WebId::class.java).handleResponse()
        var publicTypeIndexUri = webIdProfile.getPublicTypeIndex()

        if (publicTypeIndexUri == null) {
            val webIdProfile = solidResourceManager.read(URI.create(webIdProfile.getProfileUrl()),
                WebIdProfile::class.java).handleResponse()
            publicTypeIndexUri = webIdProfile.getPublicTypeIndex()
        }

       return solidResourceManager.read(URI.create(publicTypeIndexUri), SettingTypeIndex::class.java).handleResponse()
    }

    suspend fun renameContact(contactUri: String, newName: String) {
        val contactRdf = getContact(URI.create(contactUri))
        if(contactRdf.getFullName() != newName) {
            contactRdf.setFullName(newName)
            solidResourceManager.update(contactRdf)
        }
    }
}