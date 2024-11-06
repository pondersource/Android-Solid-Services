package com.pondersource.solidandroidclient.datamodules.addressbook

interface AddressBookDataModule {

    fun getAllAddressBooks(): AddressBooks

    fun getPublicAddressBooks(): List<AddressBook>

    fun getPrivateAddressBooks(): List<AddressBook>

    fun getAddressBook(addressBookUri: String): AddressBook

    fun getGroups(addressBookUri: String): List<Group>

    fun getContacts(addressBookUri: String): List<Contact>

    fun getGroup(addressBookUri: String, groupUri: String): Group
    
    fun getContact(addressBookUri: String, contact: String): Group
}