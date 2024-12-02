package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
import com.pondersource.shared.data.datamodule.contact.rdf.AddressBookRDF
import com.pondersource.shared.data.datamodule.contact.rdf.GroupsIndexRDF
import com.pondersource.shared.data.datamodule.contact.rdf.NameEmailIndexRDF
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressBook(
    val uri: String,
    var title: String,
    var contacts: List<Contact>,
    var groups: List<Group>,
): Parcelable {
    companion object {
        fun createFromRdf(
            addressBookRdf: AddressBookRDF,
            nameEmailIndexRdf: NameEmailIndexRDF,
            groupsIndexRdf: GroupsIndexRDF
        ): AddressBook {
            return AddressBook(
                uri = addressBookRdf.getIdentifier().toString(),
                title = addressBookRdf.getTitle(),
                contacts = nameEmailIndexRdf.getContacts(
                    addressBookRdf.getIdentifier().toString()
                ),
                groups = groupsIndexRdf.getGroups(
                    addressBookRdf.getIdentifier().toString()
                )
            )
        }
    }
}

@Parcelize
data class AddressBookList(
    val publicAddressBookUris: List<String>,
    val privateAddressBookUris: List<String>,
): Parcelable
