package com.erfangholami.androidsolidservices.client.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBook
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.AddressBookList
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullContact
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.FullGroup
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.NewContact
import com.erfangholami.androidsolidservices.client.internal.ANDROID_SOLID_SERVICES_DATA_MODULES_SERVICE
import com.erfangholami.androidsolidservices.client.internal.ANDROID_SOLID_SERVICES_PACKAGE_NAME
import com.erfangholami.androidsolidservices.shared.IASSDataModulesService
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.IASSContactModuleAddressBookCallback
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.IASSContactModuleAddressBookListCallback
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.IASSContactModuleFullContactCallback
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.IASSContactModuleFullGroupCallback
import com.erfangholami.androidsolidservices.shared.domain.datamodule.contact.IASSContactsModuleInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public class SolidContactsDataModule {

    public companion object {
        @Volatile
        private var INSTANCE: SolidContactsDataModule? = null

        public fun getInstance(
            context: Context,
        ): SolidContactsDataModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SolidContactsDataModule(context).also { INSTANCE = it }
            }
        }
    }

    private var iASSDataModulesService: IASSDataModulesService? = null
    private var iASSContactsModuleInterface: IASSContactsModuleInterface? = null
    private val contactsConnectionFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iASSDataModulesService = IASSDataModulesService.Stub.asInterface(service)
            iASSContactsModuleInterface = iASSDataModulesService!!.contactsDataModuleInterface
            contactsConnectionFlow.value = true

        }

        override fun onServiceDisconnected(className: ComponentName) {
            iASSDataModulesService = null
            iASSContactsModuleInterface = null
            contactsConnectionFlow.value = false
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

    public fun contactsDataModuleServiceConnectionState(): Flow<Boolean> {
        return contactsConnectionFlow
    }

    public suspend fun getAddressBooks(webId: String): AddressBookList? {
        checkService()
        return suspendCancellableCoroutine { continuation ->
            iASSContactsModuleInterface!!.getAddressBooks(webId, object :
                IASSContactModuleAddressBookListCallback.Stub() {
                override fun valueChanged(addressBookList: AddressBookList?) {
                    continuation.resume(addressBookList)
                }
            })
        }
    }

    public suspend fun createAddressBook(
        webId: String,
        title: String,
        isPrivate: Boolean = true,
        storage: String? = null,
        ownerWebId: String? = null,
        container: String? = null,
    ): AddressBook? {
        checkService()
        return suspendCancellableCoroutine { continuation ->
            iASSContactsModuleInterface!!.createAddressBook(
                webId,
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

    public suspend fun getAddressBook(webId: String, uri: String): AddressBook? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.getAddressBook(
                webId,
                uri,
                object : IASSContactModuleAddressBookCallback.Stub() {
                    override fun valueChanged(addressBook: AddressBook?) {
                        continuation.resume(addressBook)
                    }
                })
        }
    }

    public suspend fun deleteAddressBook(webId: String, addressBookUri: String): AddressBook? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.deleteAddressBook(
                webId,
                addressBookUri,
                null,
                object : IASSContactModuleAddressBookCallback.Stub() {
                    override fun valueChanged(addressBook: AddressBook?) {
                        continuation.resume(addressBook)
                    }
                })
        }
    }

    public suspend fun createNewContact(
        webId: String,
        addressBookUri: String,
        newContact: NewContact,
        groupUris: List<String> = emptyList(),
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.createNewContact(
                webId,
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

    public suspend fun getContact(webId: String, contactUri: String): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.getContact(
                webId,
                contactUri,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                })
        }
    }

    public suspend fun renameContact(webId: String, contactUri: String, newName: String): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.renameContact(
                webId,
                contactUri,
                newName,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                })
        }
    }

    public suspend fun addNewPhoneNumber(
        webId: String,
        contactUri: String,
        newPhoneNumber: String
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.addNewPhoneNumber(
                webId,
                contactUri,
                newPhoneNumber,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                })
        }
    }

    public suspend fun addNewEmailAddress(
        webId: String,
        contactUri: String,
        newEmailAddress: String
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.addNewEmailAddress(
                webId,
                contactUri,
                newEmailAddress,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                })
        }
    }

    public suspend fun removePhoneNumber(
        webId: String,
        contactUri: String,
        phoneNumber: String
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.removePhoneNumber(
                webId,
                contactUri,
                phoneNumber,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                })
        }
    }

    public suspend fun removeEmailAddress(
        webId: String,
        contactUri: String,
        emailAddress: String
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.removeEmailAddress(
                webId,
                contactUri,
                emailAddress,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                })
        }
    }

    public suspend fun deleteContact(
        webId: String,
        addressBookUri: String,
        contactUri: String
    ): FullContact? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.deleteContact(
                webId,
                addressBookUri,
                contactUri,
                object : IASSContactModuleFullContactCallback.Stub() {
                    override fun valueChanged(fullContact: FullContact?) {
                        continuation.resume(fullContact)
                    }
                })
        }
    }

    public suspend fun createNewGroup(
        webId: String,
        addressBookUri: String,
        title: String,
        contactUris: List<String> = emptyList(),
    ): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.createNewGroup(
                webId,
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

    public suspend fun getGroup(webId: String, groupUri: String): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.getGroup(
                webId,
                groupUri,
                object : IASSContactModuleFullGroupCallback.Stub() {
                    override fun valueChanged(fullGroup: FullGroup?) {
                        continuation.resume(fullGroup)
                    }
                })
        }
    }

    public suspend fun deleteGroup(webId: String, addressBookUri: String, groupUri: String): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.deleteGroup(
                webId,
                addressBookUri,
                groupUri,
                object : IASSContactModuleFullGroupCallback.Stub() {
                    override fun valueChanged(fullGroup: FullGroup?) {
                        continuation.resume(fullGroup)
                    }
                })
        }
    }

    public suspend fun addContactToGroup(webId: String, contactUri: String, groupUri: String): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.addContactToGroup(
                webId,
                contactUri,
                groupUri,
                object : IASSContactModuleFullGroupCallback.Stub() {
                    override fun valueChanged(fullGroup: FullGroup?) {
                        continuation.resume(fullGroup)
                    }
                })
        }
    }

    public suspend fun removeContactFromGroup(
        webId: String,
        contactUri: String,
        groupUri: String
    ): FullGroup? {
        checkService()
        return suspendCoroutine { continuation ->
            iASSContactsModuleInterface!!.removeContactFromGroup(
                webId,
                contactUri,
                groupUri,
                object : IASSContactModuleFullGroupCallback.Stub() {
                    override fun valueChanged(fullGroup: FullGroup?) {
                        continuation.resume(fullGroup)
                    }
                })
        }
    }
}