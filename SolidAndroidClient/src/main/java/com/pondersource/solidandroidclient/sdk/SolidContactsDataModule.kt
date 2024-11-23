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
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_CONTACTS_MODULE_SERVICE
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.pondersource.solidandroidclient.IASSContactsModuleService
import java.net.URI

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

    private var iASSContactsModuleService: IASSContactsModuleService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSContactsModuleService = IASSContactsModuleService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSContactsModuleService = null
        }
    }

    private constructor(
        context: Context,
    ) {
        val intent = Intent().apply {
            setClassName(
                ANDROID_SOLID_SERVICES_PACKAGE_NAME,
                ANDROID_SOLID_SERVICES_CONTACTS_MODULE_SERVICE
            )
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun checkService() {
        if (iASSContactsModuleService == null) {
            throw IllegalStateException("Service not connected")
        }
    }

    fun getAddressBooks(webId: String): AddressBookList {
        checkService()
        return iASSContactsModuleService!!.getAddressBooks(webId)
    }

    fun createAddressBook(
        title: String,
        storage: String,
        ownerWebId: String,
        container: String? = null,
    ) : String {
        checkService()
        return iASSContactsModuleService!!.createAddressBook(title, storage, ownerWebId, container)
    }

    fun getAddressBook(
        uri: String,
    ): AddressBook {
        checkService()
        return iASSContactsModuleService!!.getAddressBook(uri)!!
    }

    fun createNewContact(
        addressBookUri: String,
        newContact: NewContact,
        groupUris: List<String> = emptyList(),
    ) : String {
        checkService()
        return iASSContactsModuleService!!.createNewContact(addressBookUri, newContact, groupUris)
    }

    fun getContact(
        contactUri: String
    ): FullContact {
        checkService()
        return iASSContactsModuleService!!.getContact(contactUri)!!
    }

    fun renameContact(
        contactUri: String,
        newName: String,
    ) {
        checkService()
        iASSContactsModuleService!!.renameContact(contactUri, newName)
    }

    fun addNewPhoneNumber(
        contactUri: String,
        newPhoneNumber: String,
    ): FullContact {
        checkService()
        return iASSContactsModuleService!!.addNewPhoneNumber(contactUri, newPhoneNumber)
    }

    fun addNewEmailAddress(
        contactUri: String,
        newEmailAddress: String,
    ): FullContact {
        checkService()
        return iASSContactsModuleService!!.addNewEmailAddress(contactUri, newEmailAddress)
    }

    fun removePhoneNumber(
        contactUri: String,
        phoneNumber: String,
    ): Boolean {
        checkService()
        return iASSContactsModuleService!!.removePhoneNumber(contactUri, phoneNumber)
    }

    fun removeEmailAddress(
        contactUri: String,
        emailAddress: String,
    ): Boolean {
        checkService()
        return iASSContactsModuleService!!.removeEmailAddress(contactUri, emailAddress)
    }

    fun createNewGroup(
        addressBookUri: String,
        title: String,
    ): String {
        checkService()
        return iASSContactsModuleService!!.createNewGroup(addressBookUri, title)
    }

    fun getGroup(
        groupUri: String,
    ): FullGroup {
        checkService()
        return iASSContactsModuleService!!.getGroup(groupUri)
    }
    fun removeGroup(
        addressBookUri: String,
        groupUri: String
    ): Boolean {
        checkService()
        return iASSContactsModuleService!!.removeGroup(addressBookUri, groupUri)
    }

    fun addContactToGroup(
        contactUri: String,
        groupUri: String,
    ) {
        checkService()
        return iASSContactsModuleService!!.addContactToGroup(contactUri, groupUri)
    }

    fun removeContactFromGroup(
        contactUri: String,
        groupUri: String,
    ): Boolean {
        checkService()
        return iASSContactsModuleService!!.removeContactFromGroup(contactUri, groupUri)
    }
}