package com.pondersource.shared.data.datamodule.contact

import java.net.URI

data class Group(
    val uri: URI,
    val name: String,
)

data class FullGroup(
    val uri: URI,
    val name: String,
    val contacts: List<Contact>,
)
