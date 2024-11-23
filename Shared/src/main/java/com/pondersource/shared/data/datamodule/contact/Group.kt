package com.pondersource.shared.data.datamodule.contact

import android.os.Parcelable
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
): Parcelable
