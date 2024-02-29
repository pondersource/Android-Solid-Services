package com.pondersource.solidandroidclient

import android.util.Log
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.auth.Session
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidNonRDFSource
import com.inrupt.client.solid.SolidSyncClient
import org.apache.http.HttpHeaders.ACCEPT
import java.net.URI

class SolidCRUD {

    companion object {
        private const val TAG = "SolidCRUD"
        @Volatile
        private lateinit var INSTANCE: SolidCRUD

        fun getInstance(authenticator: Authenticator): SolidCRUD {
            return if (::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidCRUD(authenticator)
                INSTANCE
            }
        }
    }

    private val authenticator: Authenticator
    private lateinit var session: Session
    private lateinit var client: SolidSyncClient

    private constructor(authenticator: Authenticator) {
        this.authenticator = authenticator
    }

    fun test() {
        this.session = OpenIdSession.ofIdToken(authenticator.getAuthState().idToken)
        this.client = SolidSyncClient.getClient().session(session)

        if (session.principal != null) {

            session.principal.ifPresent { webid: URI? ->


                val myNonRDFFile = SolidNonRDFSource(
                    URI.create("https://storage.inrupt.com/4a1ea008-8f12-4451-8cce-1f6f0be6b2ce/hello.png")
                        .normalize(),
                    "text/image",
                    "Hello solid".byteInputStream() // to input stream
                )
                val xx = client.create(myNonRDFFile).getIdentifier().toString()
                Log.d(TAG, xx)


                val x = client.read(webid, SolidNonRDFSource::class.java)
                Log.d(TAG, "Translate is: " + String(x.entity.readBytes()))

                val webIdRequest: Request = Request.newBuilder()
                    .header(ACCEPT, "application/ld+json")
                    .uri(webid)
                    .GET()
                    .build()
                val webIdResponse: Response<String> =
                    client.send(webIdRequest, Response.BodyHandlers.ofString())


                val profileRequest: Request = Request.newBuilder()
                    .header(ACCEPT, "application/ld+json")
                    .uri(URI("https://storage.inrupt.com/4a1ea008-8f12-4451-8cce-1f6f0be6b2ce/profile"))
                    .GET()
                    .build()
                val profileResponse: Response<String> =
                    client.send(profileRequest, Response.BodyHandlers.ofString())

                val imageRequest: Request = Request.newBuilder()
                    .uri(URI("https://storage.inrupt.com/4a1ea008-8f12-4451-8cce-1f6f0be6b2ce/hello.png"))
                    .GET()
                    .build()
                val imageResponse: Response<String> =
                    client.send(imageRequest, Response.BodyHandlers.ofString())

                val webId: WebId = client.get(URI(webid.toString()), WebId::class.java)

                Log.d(TAG,"HTTP status code: " + imageResponse.statusCode())
            }
        }
    }
}