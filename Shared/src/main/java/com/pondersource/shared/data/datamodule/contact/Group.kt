package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URI

@Parcelize
data class Group(
    val uri: URI,
    val name: String,
): Parcelable

@Parcelize
data class FullGroup(
    val uri: URI,
    val name: String,
    val contacts: List<Contact>,
): Parcelable
