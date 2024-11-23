package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressBook(
    val uri: String,
    var title: String,
    var contacts: List<Contact>,
    var groups: List<Group>,
): Parcelable

@Parcelize
data class AddressBookList(
    val publicAddressBookUris: List<String>,
    val privateAddressBookUris: List<String>,
): Parcelable
