package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.shared.data.datamodule.getOrNull
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

        private fun getProfile() = auth.getProfile()

        override fun getAddressBooks(callback: IASSContactModuleAddressBookListCallback) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(solidContactsDataModule.getAddressBooks(auth.getProfile().userInfo!!.webId).getOrNull())
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
                        ownerWebId ?: getProfile().userInfo!!.webId, //TODO
                        title,
                        isPrivate,
                        storage ?: auth.getProfile().webId!!.getStorages().get(0).toString(), //TODO

                        container
                    ).getOrNull()
                )
            }
        }

        override fun getAddressBook(
            uri: String,
            callback: IASSContactModuleAddressBookCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getAddressBook(
                        getProfile().userInfo!!.webId,
                        uri
                    ).getOrNull()
                )
            }
        }

        override fun deleteAddressBook(
            uri: String,
            ownerWebId: String?,
            callback: IASSContactModuleAddressBookCallback
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.deleteAddressBook(
                        uri,
                        ownerWebId ?:  auth.getProfile().userInfo!!.webId,
                    ).getOrNull()
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
                        getProfile().userInfo!!.webId,
                        addressBookUri,
                        newContact,
                        groupUris
                    ).getOrNull()
                )
            }
        }

        override fun getContact(
            contactUri: String,
            callback: IASSContactModuleFullContactCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getContact(
                        getProfile().userInfo!!.webId,
                        contactUri
                    ).getOrNull()
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
                    solidContactsDataModule.renameContact(
                        getProfile().userInfo!!.webId,
                        contactUri,
                        newName
                    ).getOrNull()
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
                    solidContactsDataModule.addNewPhoneNumber(
                        getProfile().userInfo!!.webId,
                        contactUri,
                        newPhoneNumber
                    ).getOrNull()
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
                    solidContactsDataModule.addNewEmailAddress(
                        getProfile().userInfo!!.webId,
                        contactUri,
                        newEmailAddress
                    ).getOrNull()
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
                    solidContactsDataModule.removePhoneNumber(
                        getProfile().userInfo!!.webId,
                        contactUri,
                        phoneNumber
                    ).getOrNull()
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
                    solidContactsDataModule.removeEmailAddress(
                        getProfile().userInfo!!.webId,
                        contactUri,
                        emailAddress
                    ).getOrNull()
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
                        getProfile().userInfo!!.webId,
                        addressBookUri,
                        contactUri,
                    ).getOrNull()
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
                    solidContactsDataModule.createNewGroup(
                        getProfile().userInfo!!.webId,
                        addressBookUri,
                        title,
                        contactUris
                    ).getOrNull()
                )
            }
        }

        override fun getGroup(
            groupUri: String,
            callback: IASSContactModuleFullGroupCallback,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                callback.valueChanged(
                    solidContactsDataModule.getGroup(
                        getProfile().userInfo!!.webId,
                        groupUri
                    ).getOrNull()
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
                    solidContactsDataModule.deleteGroup(
                        getProfile().userInfo!!.webId,
                        addressBookUri,
                        groupUri
                    ).getOrNull()
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
                    solidContactsDataModule.addContactToGroup(
                        getProfile().userInfo!!.webId,
                        contactUri,
                        groupUri
                    ).getOrNull()
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
                    solidContactsDataModule.removeContactFromGroup(
                        getProfile().userInfo!!.webId,
                        contactUri,
                        groupUri
                    ).getOrNull()
                )
            }
        }
    }
}