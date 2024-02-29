package com.pondersource.solidandroidclient

import com.google.gson.Gson
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.solid.SolidSyncClient
import org.apache.http.HttpHeaders
import java.net.URI

fun <T> SolidSyncClient.get(uri: URI, clazz: Class<T>): T {
    val request: Request = Request.newBuilder()
        .header(HttpHeaders.ACCEPT, "application/ld+json")
        .uri(uri)
        .GET()
        .build()

    val response: Response<String> = this.send(request, Response.BodyHandlers.ofString())

    return Gson().fromJson(response.body(), clazz)
}