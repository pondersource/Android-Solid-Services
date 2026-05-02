package com.pondersource.shared.domain.util

import java.io.InputStream

public fun InputStream.toPlainString(): String {
    return this.bufferedReader().use { it.readText() }
}