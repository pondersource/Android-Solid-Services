package com.pondersource.solidandroidclient.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.pondersource.shared.data.datamodule.contact.AddressBook
import com.pondersource.shared.data.datamodule.contact.AddressBookList
import com.pondersource.shared.data.datamodule.contact.FullContact
import com.pondersource.shared.data.datamodule.contact.FullGroup
import com.pondersource.shared.data.datamodule.contact.NewContact
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_DATA_MODULES_SERVICE
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.pondersource.solidandroidclient.IASSContactsModuleInterface
import com.pondersource.solidandroidclient.IASSDataModulesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class SolidContactsDataModule {

    companion object {
        @Volatile
        private lateinit var INSTANCE: SolidContactsDataModule

        fun getInstance(
            context: Context,
        ): SolidContactsDataModule {
            return if (Companion::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidContactsDataModule(context)
                INSTANCE
            }
        }
    }

    private var iASSDataModulesService: IASSDataModulesService? = null
    private var iASSContactsModuleInterface: IASSContactsModuleInterface? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSDataModulesService = IASSDataModulesService.Stub.asInterface(service)
            iASSContactsModuleInterface = iASSDataModulesService!!.contactsDataModuleInterface

        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSDataModulesService = null
            iASSContactsModuleInterface = null
        }
    }

    private constructor(
        context: Context,
    ) {
        val intent = Intent().apply {
            setClassName(
                ANDROID_SOLID_SERVICES_PACKAGE_NAME,
                ANDROID_SOLID_SERVICES_DATA_MODULES_SERVICE
            )
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun checkService() {
        if (iASSContactsModuleInterface == null) {
            throw IllegalStateException("Service not connected")
        }
    }

    fun getAddressBooks(): AddressBookList? {
        return runBlocking(Dispatchers.IO) {
            delay(2000L)
            checkService()
            iASSContactsModuleInterface!!.getAddressBooks()
        }
    }

    fun createAddressBook(
        title: String,
        storage: String,
        ownerWebId: String,
        container: String? = null,
    ) : AddressBook? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.createAddressBook(
                title,
                storage,
                ownerWebId,
                container
            )
        }
    }

    fun getAddressBook(
        uri: String,
    ): AddressBook? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.getAddressBook(uri)
        }
    }

    fun createNewContact(
        addressBookUri: String,
        newContact: NewContact,
        groupUris: List<String> = emptyList(),
    ) : FullContact? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.createNewContact(
                addressBookUri,
                newContact,
                groupUris
            )
        }
    }

    fun getContact(
        contactUri: String
    ): FullContact? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.getContact(contactUri)!!
        }
    }

    fun renameContact(
        contactUri: String,
        newName: String,
    ): FullContact? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.renameContact(contactUri, newName)
        }
    }

    fun addNewPhoneNumber(
        contactUri: String,
        newPhoneNumber: String,
    ): FullContact? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.addNewPhoneNumber(contactUri, newPhoneNumber)
        }
    }

    fun addNewEmailAddress(
        contactUri: String,
        newEmailAddress: String,
    ): FullContact? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.addNewEmailAddress(contactUri, newEmailAddress)
        }
    }

    fun removePhoneNumber(
        contactUri: String,
        phoneNumber: String,
    ): FullContact? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.removePhoneNumber(contactUri, phoneNumber)
        }
    }

    fun removeEmailAddress(
        contactUri: String,
        emailAddress: String,
    ): FullContact? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.removeEmailAddress(contactUri, emailAddress)
        }
    }

    fun createNewGroup(
        addressBookUri: String,
        title: String,
    ): FullGroup? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.createNewGroup(addressBookUri, title)
        }
    }

    fun getGroup(
        groupUri: String,
    ): FullGroup? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.getGroup(groupUri)
        }
    }
    fun removeGroup(
        addressBookUri: String,
        groupUri: String
    ): FullGroup? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.removeGroup(addressBookUri, groupUri)
        }
    }

    fun addContactToGroup(
        contactUri: String,
        groupUri: String,
    ): FullGroup? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.addContactToGroup(contactUri, groupUri)
        }
    }

    fun removeContactFromGroup(
        contactUri: String,
        groupUri: String,
    ): FullGroup? {
        return runBlocking(Dispatchers.IO) {
            checkService()
            iASSContactsModuleInterface!!.removeContactFromGroup(contactUri, groupUri)
        }
    }
}