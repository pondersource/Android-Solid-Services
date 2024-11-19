package com.pondersource.shared.data.datamodule.contact

import java.net.URI

data class AddressBook(
    val uri: URI,
    var title: String,
    var contacts: List<Contact>,
    var groups: List<Group>,
)

data class AddressBookList(
    val publicAddressBookUris: List<URI>,
    val privateAddressBookUris: List<URI>,
)
