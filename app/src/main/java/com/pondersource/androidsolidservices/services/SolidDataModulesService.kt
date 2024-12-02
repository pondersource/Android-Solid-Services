package com.pondersource.androidsolidservices.services

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.shared.data.datamodule.extractResult
import com.pondersource.solidandroidapi.Authenticator
import com.pondersource.solidandroidapi.datamodule.SolidContactsDataModule
import com.pondersource.solidandroidclient.IASSContactsModuleInterface
import com.pondersource.solidandroidclient.IASSDataModulesService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

        override fun getAddressBooks(): AddressBookList? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getAddressBooks(auth.getProfile().userInfo!!.webId).extractResult()
            }
        }

        override fun createAddressBook(
            title: String,
            storage: String,
            ownerWebId: String,
            container: String?
        ): AddressBook? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.createAddressBook(
                    title,
                    storage,
                    true,
                    ownerWebId,
                    container
                ).extractResult()
            }
        }

        override fun getAddressBook(
            uri: String
        ): AddressBook? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getAddressBook(uri).extractResult()
            }
        }

        override fun createNewContact(
            addressBookUri: String,
            newContact: NewContact,
            groupUris: List<String>
        ): FullContact? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.createNewContact(
                    addressBookUri,
                    newContact,
                    groupUris
                ).extractResult()
            }
        }

        override fun getContact(
            contactUri: String
        ): FullContact? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getContact(contactUri).extractResult()
            }
        }

        override fun renameContact(
            contactUri: String,
            newName: String
        ): FullContact? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.renameContact(contactUri, newName).extractResult()
            }
        }

        override fun addNewPhoneNumber(
            contactUri: String,
            newPhoneNumber: String
        ): FullContact? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.addNewPhoneNumber(contactUri, newPhoneNumber).extractResult()
            }
        }

        override fun addNewEmailAddress(
            contactUri: String,
            newEmailAddress: String
        ): FullContact? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.addNewEmailAddress(contactUri, newEmailAddress).extractResult()
            }
        }

        override fun removePhoneNumber(
            contactUri: String,
            phoneNumber: String
        ): FullContact? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removePhoneNumber(contactUri, phoneNumber).extractResult()
            }
        }

        override fun removeEmailAddress(
            contactUri: String,
            emailAddress: String
        ): FullContact? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removeEmailAddress(contactUri, emailAddress).extractResult()
            }
        }

        override fun createNewGroup(
            addressBookUri: String,
            title: String
        ): FullGroup? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.createNewGroup(addressBookUri, title).extractResult()
            }
        }

        override fun getGroup(
            groupUri: String
        ): FullGroup? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.getGroup(groupUri).extractResult()
            }
        }

        override fun removeGroup(
            addressBookUri: String,
            groupUri: String
        ): FullGroup? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removeGroup(addressBookUri, groupUri).extractResult()
            }
        }

        override fun addContactToGroup(
            contactUri: String,
            groupUri: String
        ): FullGroup? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.addContactToGroup(contactUri, groupUri).extractResult()
            }
        }

        override fun removeContactFromGroup(
            contactUri: String,
            groupUri: String
        ): FullGroup? {
            return runBlocking(Dispatchers.IO) {
                solidContactsDataModule.removeContactFromGroup(contactUri, groupUri).extractResult()
            }
        }
    }
}