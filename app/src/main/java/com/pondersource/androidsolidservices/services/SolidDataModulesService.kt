package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pondersource.shared.domain.datamodule.contact.NewContact
import com.pondersource.shared.domain.datamodule.getOrNull
import com.pondersource.solidandroidapi.Authenticator
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModule
import com.pondersource.shared.IASSDataModulesService
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleAddressBookCallback
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleAddressBookListCallback
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleFullContactCallback
import com.pondersource.shared.domain.datamodule.contact.IASSContactModuleFullGroupCallback
import com.pondersource.shared.domain.datamodule.contact.IASSContactsModuleInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SolidDataModulesService : LifecycleService() {

    @Inject
    lateinit var solidContactsDataModule: SolidContactsDataModule

    @Inject
    lateinit var auth: Authenticator

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    //One service for all data modules
    private val binder = object : IASSDataModulesService.Stub() {
        override fun getContactsDataModuleInterface(): IASSContactsModuleInterface {
            return contactsModuleInterface
        }

    }

    private val contactsModuleInterface = object : IASSContactsModuleInterface.Stub() {

        override fun getAddressBooks(webId: String, callback: IASSContactModuleAddressBookListCallback) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getAddressBooks(webId).getOrNull()
                )
            }
        }

        override fun createAddressBook(
            webId: String,
            title: String,
            isPrivate: Boolean,
            callback: IASSContactModuleAddressBookCallback,
            storage: String?,
            ownerWebId: String?,
            container: String?
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                val profile = auth.getProfile(webId)
                callback.valueChanged(
                    solidContactsDataModule.createAddressBook(
                        ownerWebId ?: webId,
                        title,
                        isPrivate,
                        storage ?: profile.webId!!.getStorages()[0].toString(),
                        container
                    ).getOrNull()
                )
            }
        }

        override fun getAddressBook(
            webId: String,
            uri: String,
            callback: IASSContactModuleAddressBookCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getAddressBook(webId, uri).getOrNull()
                )
            }
        }

        override fun deleteAddressBook(
            webId: String,
            uri: String,
            ownerWebId: String?,
            callback: IASSContactModuleAddressBookCallback
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.deleteAddressBook(uri, ownerWebId ?: webId).getOrNull()
                )
            }
        }

        override fun createNewContact(
            webId: String,
            addressBookUri: String,
            newContact: NewContact,
            groupUris: List<String>,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.createNewContact(webId, addressBookUri, newContact, groupUris).getOrNull()
                )
            }
        }

        override fun getContact(
            webId: String,
            contactUri: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getContact(webId, contactUri).getOrNull()
                )
            }
        }

        override fun renameContact(
            webId: String,
            contactUri: String,
            newName: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.renameContact(webId, contactUri, newName).getOrNull()
                )
            }
        }

        override fun addNewPhoneNumber(
            webId: String,
            contactUri: String,
            newPhoneNumber: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.addNewPhoneNumber(webId, contactUri, newPhoneNumber).getOrNull()
                )
            }
        }

        override fun addNewEmailAddress(
            webId: String,
            contactUri: String,
            newEmailAddress: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.addNewEmailAddress(webId, contactUri, newEmailAddress).getOrNull()
                )
            }
        }

        override fun removePhoneNumber(
            webId: String,
            contactUri: String,
            phoneNumber: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.removePhoneNumber(webId, contactUri, phoneNumber).getOrNull()
                )
            }
        }

        override fun removeEmailAddress(
            webId: String,
            contactUri: String,
            emailAddress: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.removeEmailAddress(webId, contactUri, emailAddress).getOrNull()
                )
            }
        }

        override fun deleteContact(
            webId: String,
            addressBookUri: String,
            contactUri: String,
            callback: IASSContactModuleFullContactCallback
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.deleteContact(webId, addressBookUri, contactUri).getOrNull()
                )
            }
        }

        override fun createNewGroup(
            webId: String,
            addressBookUri: String,
            title: String,
            contactUris: List<String>,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.createNewGroup(webId, addressBookUri, title, contactUris).getOrNull()
                )
            }
        }

        override fun getGroup(
            webId: String,
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getGroup(webId, groupUri).getOrNull()
                )
            }
        }

        override fun deleteGroup(
            webId: String,
            addressBookUri: String,
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.deleteGroup(webId, addressBookUri, groupUri).getOrNull()
                )
            }
        }

        override fun addContactToGroup(
            webId: String,
            contactUri: String,
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.addContactToGroup(webId, contactUri, groupUri).getOrNull()
                )
            }
        }

        override fun removeContactFromGroup(
            webId: String,
            contactUri: String,
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.removeContactFromGroup(webId, contactUri, groupUri).getOrNull()
                )
            }
        }
    }
}