package com.pondersource.solidandroidclient.datamodules.addressbook

import com.google.gson.annotations.SerializedName

data class AddressBooks(
    val publicAddressBooks: List<AddressBook>,
    val privateAddressBooks: List<AddressBook>,
)

data class AddressBook(
    val contacts: List<Contact>,
    val groups: List<Group>,
)

data class Contact(
    @SerializedName("https://schema.org/name")
    val name: String,
    @SerializedName("https://schema.org/telephone")
    val phoneNumber: String,
)

data class Group(
    val title: String,
    val contact: List<Contact>
)