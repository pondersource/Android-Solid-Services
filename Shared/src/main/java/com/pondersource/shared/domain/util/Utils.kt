package com.pondersource.shared.domain.util

import java.io.InputStream

fun InputStream.toPlainString(): String {
    return this.bufferedReader().use { it.readText() }
}