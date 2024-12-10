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
import com.pondersource.solidandroidclient.IASSDataModulesService
import com.pondersource.solidandroidclient.contacts.IASSContactModuleAddressBookCallback
import com.pondersource.solidandroidclient.contacts.IASSContactModuleAddressBookListCallback
import com.pondersource.solidandroidclient.contacts.IASSContactModuleFullContactCallback
import com.pondersource.solidandroidclient.contacts.IASSContactModuleFullGroupCallback
import com.pondersource.solidandroidclient.contacts.IASSContactsModuleInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    private val ContactsConnectionFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSDataModulesService = IASSDataModulesService.Stub.asInterface(service)
            iASSContactsModuleInterface = iASSDataModulesService!!.contactsDataModuleInterface
            ContactsConnectionFlow.value = true

        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSDataModulesService = null
            iASSContactsModuleInterface = null
            ContactsConnectionFlow.value = false
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
            throw SolidException.SolidServiceConnectionException()
        }
    }

    fun contactsDataModuleServiceConnectionState(): Flow<Boolean> {
        return ContactsConnectionFlow
    }

    suspend fun getAddressBooks(): AddressBookList? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.getAddressBooks(object: IASSContactModuleAddressBookListCallback.Stub() {
                override fun valueChanged(addressBookList: AddressBookList?) {
                    continuation.resume(addressBookList)
                }
            })
        }
    }

    suspend fun createAddressBook(
        title: String,
        isPrivate: Boolean = true,
        storage: String? = null,
        ownerWebId: String? = null,
        container: String? = null,
    ) : AddressBook? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.createAddressBook(
                title,
                isPrivate,
                object : IASSContactModuleAddressBookCallback.Stub() {
                    override fun valueChanged(addressBook: AddressBook?) {
                        continuation.resume(addressBook)
                    }
                },
                storage,
                ownerWebId,
                container
            )
        }
    }

    suspend fun getAddressBook(
        uri: String,
    ): AddressBook? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.getAddressBook(uri, object : IASSContactModuleAddressBookCallback.Stub() {
                override fun valueChanged(addressBook: AddressBook?) {
                    continuation.resume(addressBook)
                }
            })
        }
    }

    suspend fun deleteAddressBook(
        addressBookUri: String,
    ): AddressBook? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.deleteAddressBood(addressBookUri, null, object : IASSContactModuleAddressBookCallback.Stub() {
                override fun valueChanged(addressBook: AddressBook?) {
                    continuation.resume(addressBook)
                }
            })
        }
    }

    suspend fun createNewContact(
        addressBookUri: String,
        newContact: NewContact,
        groupUris: List<String> = emptyList(),
    ) : FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.createNewContact(
                addressBookUri,
                newContact,
                groupUris,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                }
            )
        }
    }

    suspend fun getContact(
        contactUri: String
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.getContact(contactUri, object : IASSContactModuleFullContactCallback.Stub() {
                override fun valueChanged(fullContact: FullContact?) {
                    continuation.resume(fullContact)
                }
            })
        }

    }

    suspend fun renameContact(
        contactUri: String,
        newName: String,
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.renameContact(contactUri, newName, object : IASSContactModuleFullContactCallback.Stub() {
                override fun valueChanged(fullContact: FullContact?) {
                    continuation.resume(fullContact)
                }
            })
        }
    }

    suspend fun addNewPhoneNumber(
        contactUri: String,
        newPhoneNumber: String,
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.addNewPhoneNumber(contactUri, newPhoneNumber, object : IASSContactModuleFullContactCallback.Stub() {
                override fun valueChanged(fullContact: FullContact?) {
                    continuation.resume(fullContact)
                }
            })
        }
    }

    suspend fun addNewEmailAddress(
        contactUri: String,
        newEmailAddress: String,
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.addNewEmailAddress(contactUri, newEmailAddress, object : IASSContactModuleFullContactCallback.Stub() {
                override fun valueChanged(fullContact: FullContact?) {
                    continuation.resume(fullContact)
                }
            })
        }
    }

    suspend fun removePhoneNumber(
        contactUri: String,
        phoneNumber: String,
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.removePhoneNumber(contactUri, phoneNumber, object : IASSContactModuleFullContactCallback.Stub() {
                override fun valueChanged(fullContact: FullContact?) {
                    continuation.resume(fullContact)
                }
            })
        }
    }

    suspend fun removeEmailAddress(
        contactUri: String,
        emailAddress: String,
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.removeEmailAddress(contactUri, emailAddress, object : IASSContactModuleFullContactCallback.Stub() {
                override fun valueChanged(fullContact: FullContact?) {
                    continuation.resume(fullContact)
                }
            })
        }
    }

    suspend fun deleteContact(
        addressBookUri: String,
        contactUri: String
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.deleteContact(addressBookUri, contactUri, object : IASSContactModuleFullContactCallback.Stub() {
                override fun valueChanged(fullContact: FullContact?) {
                    continuation.resume(fullContact)
                }
            })
        }
    }

    suspend fun createNewGroup(
        addressBookUri: String,
        title: String,
        contactUris: List<String> = emptyList(),
    ): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.createNewGroup(
                addressBookUri,
                title,
                contactUris,
                object : IASSContactModuleFullGroupCallback.Stub() {
                override fun valueChanged(fullGroup: FullGroup?) {
                    continuation.resume(fullGroup)
                }
            })
        }
    }

    suspend fun getGroup(
        groupUri: String,
    ): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.getGroup(groupUri, object : IASSContactModuleFullGroupCallback.Stub() {
                override fun valueChanged(fullGroup: FullGroup?) {
                    continuation.resume(fullGroup)
                }
            })
        }
    }

    suspend fun deleteGroup(
        addressBookUri: String,
        groupUri: String
    ): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.deleteGroup(addressBookUri, groupUri, object : IASSContactModuleFullGroupCallback.Stub() {
                override fun valueChanged(fullGroup: FullGroup?) {
                    continuation.resume(fullGroup)
                }
            })
        }
    }

    suspend fun addContactToGroup(
        contactUri: String,
        groupUri: String,
    ): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.addContactToGroup(contactUri, groupUri, object : IASSContactModuleFullGroupCallback.Stub() {
                override fun valueChanged(fullGroup: FullGroup?) {
                    continuation.resume(fullGroup)
                }
            })
        }
    }

    suspend fun removeContactFromGroup(
        contactUri: String,
        groupUri: String,
    ): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.removeContactFromGroup(contactUri, groupUri, object : IASSContactModuleFullGroupCallback.Stub() {
                override fun valueChanged(fullGroup: FullGroup?) {
                    continuation.resume(fullGroup)
                }
            })
        }
    }
}