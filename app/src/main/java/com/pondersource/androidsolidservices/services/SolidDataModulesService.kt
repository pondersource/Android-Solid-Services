package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.shared.data.datamodule.extractResult
import com.pondersource.solidandroidapi.Authenticator
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModule
import com.pondersource.solidandroidclient.IASSDataModulesService
import com.pondersource.solidandroidclient.contacts.IASSContactModuleAddressBookCallback
import com.pondersource.solidandroidclient.contacts.IASSContactModuleAddressBookListCallback
import com.pondersource.solidandroidclient.contacts.IASSContactModuleFullContactCallback
import com.pondersource.solidandroidclient.contacts.IASSContactModuleFullGroupCallback
import com.pondersource.solidandroidclient.contacts.IASSContactsModuleInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SolidDataModulesService : LifecycleService() {

    @Inject
    lateinit var solidContactsDataModule : SolidContactsDataModule

    @Inject
    lateinit var auth : Authenticator

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    //One service for all data modules
    private val binder = object : IASSDataModulesService.Stub() {
        override fun getContactsDataModuleInterface(): IASSContactsModuleInterface? {
           return contactsModuleInterface
        }

    }

    private val contactsModuleInterface = object : IASSContactsModuleInterface.Stub() {

        override fun getAddressBooks(callback: IASSContactModuleAddressBookListCallback) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(solidContactsDataModule.getAddressBooks(auth.getProfile().userInfo!!.webId).extractResult())
            }
        }

        override fun createAddressBook(
            title: String,
            isPrivate: Boolean,
            callback: IASSContactModuleAddressBookCallback,
            storage: String?,
            ownerWebId: String?,
            container: String?
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.createAddressBook(
                        title,
                        isPrivate,
                        storage ?: auth.getProfile().webId!!.getStorages().get(0).toString(), //TODO
                        ownerWebId ?: auth.getProfile().userInfo!!.webId, //TODO
                        container
                    ).extractResult()
                )
            }
        }

        override fun getAddressBook(
            uri: String,
            callback: IASSContactModuleAddressBookCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getAddressBook(uri).extractResult()
                )
            }
        }

        override fun deleteAddressBood(
            uri: String,
            ownerWebId: String?,
            callback: IASSContactModuleAddressBookCallback
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.deleteAddressBook(
                        uri,
                        ownerWebId ?:  auth.getProfile().userInfo!!.webId,
                    ).extractResult()
                )
            }
        }

        override fun createNewContact(
            addressBookUri: String,
            newContact: NewContact,
            groupUris: List<String>,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.createNewContact(
                        addressBookUri,
                        newContact,
                        groupUris
                    ).extractResult()
                )
            }
        }

        override fun getContact(
            contactUri: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getContact(contactUri).extractResult()
                )
            }
        }

        override fun renameContact(
            contactUri: String,
            newName: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.renameContact(contactUri, newName).extractResult()
                )
            }
        }

        override fun addNewPhoneNumber(
            contactUri: String,
            newPhoneNumber: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.addNewPhoneNumber(contactUri, newPhoneNumber).extractResult()
                )
            }
        }

        override fun addNewEmailAddress(
            contactUri: String,
            newEmailAddress: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.addNewEmailAddress(contactUri, newEmailAddress).extractResult()
                )
            }
        }

        override fun removePhoneNumber(
            contactUri: String,
            phoneNumber: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.removePhoneNumber(contactUri, phoneNumber).extractResult()
                )
            }
        }

        override fun removeEmailAddress(
            contactUri: String,
            emailAddress: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.removeEmailAddress(contactUri, emailAddress).extractResult()
                )
            }
        }

        override fun deleteContact(
            addressBookUri: String,
            contactUri: String,
            callback: IASSContactModuleFullContactCallback
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.deleteContact(
                        addressBookUri,
                        contactUri,
                    ).extractResult()
                )
            }
        }

        override fun createNewGroup(
            addressBookUri: String,
            title: String,
            contactUris: List<String>,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.createNewGroup(addressBookUri, title, contactUris).extractResult()
                )
            }
        }

        override fun getGroup(
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getGroup(groupUri).extractResult()
                )
            }
        }

        override fun deleteGroup(
            addressBookUri: String,
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.deleteGroup(addressBookUri, groupUri).extractResult()
                )
            }
        }

        override fun addContactToGroup(
            contactUri: String,
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.addContactToGroup(contactUri, groupUri).extractResult()
                )
            }
        }

        override fun removeContactFromGroup(
            contactUri: String,
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.removeContactFromGroup(contactUri, groupUri).extractResult()
                )
            }
        }
    }
}