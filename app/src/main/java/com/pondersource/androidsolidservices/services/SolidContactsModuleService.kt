package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModule
import com.pondersource.solidandroidclient.IASSContactsModuleService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class SolidContactsModuleService : LifecycleService() {

    @Inject
    lateinit var solidContactsDataModule : SolidContactsDataModule

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    private val binder = object : IASSContactsModuleService.Stub() {
        override fun createAddressBook(
            title: String,
            storage: String,
            ownerWebId: String,
            container: String?
        ): String {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.createAddressBook(
                    title,
                    storage,
                    ownerWebId,
                    container
                ).toString()
            }
        }

        override fun getAddressBooks(
            webId: String
        ): AddressBookList {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getAddressBooks(webId)
            }
        }

        override fun getAddressBook(
            uri: String
        ): AddressBook {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getAddressBook(uri)
            }
        }

        override fun createNewContact(
            addressBookUri: String,
            newContact: NewContact,
            groupUris: List<String>
        ): String {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.createNewContact(
                    addressBookUri,
                    newContact,
                    groupUris
                ).toString()
            }
        }

        override fun getContact(
            contactUri: String
        ): FullContact {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getContact(contactUri)
            }
        }

        override fun renameContact(
            contactUri: String,
            newName: String
        ) {
            runBlocking(Dispatchers.IO) {
                solidContactsDataModule.renameContact(contactUri, newName)
            }
        }

        override fun addNewPhoneNumber(
            contactUri: String,
            newPhoneNumber: String
        ): FullContact {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.addNewPhoneNumber(contactUri, newPhoneNumber)
            }
        }

        override fun addNewEmailAddress(
            contactUri: String,
            newEmailAddress: String
        ): FullContact {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.addNewEmailAddress(contactUri, newEmailAddress)
            }
        }

        override fun removePhoneNumber(
            contactUri: String,
            phoneNumber: String
        ): Boolean {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removePhoneNumber(contactUri, phoneNumber)
            }
        }

        override fun removeEmailAddress(
            contactUri: String,
            emailAddress: String
        ): Boolean {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removeEmailAddress(contactUri, emailAddress)
            }
        }

        override fun createNewGroup(
            addressBookUri: String,
            title: String
        ): String {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.createNewGroup(addressBookUri, title).toString()
            }
        }

        override fun getGroup(
            groupUri: String
        ): FullGroup {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getGroup(groupUri)
            }
        }

        override fun removeGroup(
            addressBookUri: String,
            groupUri: String
        ): Boolean {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removeGroup(addressBookUri, groupUri)
            }
        }

        override fun addContactToGroup(
            contactUri: String,
            groupUri: String
        ) {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.addContactToGroup(contactUri, groupUri)
            }
        }

        override fun removeContactFromGroup(
            contactUri: String,
            groupUri: String
        ): Boolean {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removeContactFromGroup(contactUri, groupUri)
            }
        }
    }
}