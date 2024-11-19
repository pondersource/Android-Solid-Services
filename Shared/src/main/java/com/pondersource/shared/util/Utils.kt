package com.pondersource.shared.util

import java.io.InputStream

fun InputStream.toPlainString(): String {
    return this.bufferedReader().use { it.readText() }
}