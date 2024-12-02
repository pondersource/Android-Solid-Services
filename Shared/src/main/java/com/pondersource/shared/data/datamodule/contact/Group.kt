package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
import com.pondersource.shared.data.datamodule.contact.rdf.GroupRDF
import kotlinx.parcelize.Parcelize

@Parcelize
data class Group(
    val uri: String,
    val name: String,
): Parcelable

@Parcelize
data class FullGroup(
    val uri: String,
    val name: String,
    val contacts: List<Contact>,
): Parcelable {
    companion object {
        fun createFromRdf(groupRdf: GroupRDF): FullGroup {
            return FullGroup(
                uri = groupRdf.getIdentifier().toString(),
                name = groupRdf.getTitle(),
                contacts = groupRdf.getContacts()
            )
        }
    }
}
