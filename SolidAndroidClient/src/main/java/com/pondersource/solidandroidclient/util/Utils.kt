package com.pondersource.solidandroidclient.util

import java.io.InputStream

fun InputStream.toPlainString(): String {
    return this.bufferedReader().use { it.readText() }
}