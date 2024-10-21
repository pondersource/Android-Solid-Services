package com.pondersource.solidandroidclient.util

import com.inrupt.client.Response

fun <T> Response<T>.isSuccessful(): Boolean {
    return this.statusCode() in 200..299
}

fun <T> Response<T>.isNotFound(): Boolean {
    return this.statusCode() == 404
}

