package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URI

@Parcelize
data class AddressBook(
    val uri: URI,
    var title: String,
    var contacts: List<Contact>,
    var groups: List<Group>,
): Parcelable

@Parcelize
data class AddressBookList(
    val publicAddressBookUris: List<URI>,
    val privateAddressBookUris: List<URI>,
): Parcelable
