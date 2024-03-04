package com.pondersource.solidandroidclient

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.inrupt.client.Client
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.auth.Session
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidNonRDFSource
import com.inrupt.client.solid.SolidSyncClient
import com.pondersource.solidandroidclient.data.UserInfo
import com.pondersource.solidandroidclient.data.fromJsonStringToUserInfo
import kotlinx.coroutines.delay
import org.apache.http.HttpHeaders.ACCEPT
import java.net.URI

class SolidCRUD private constructor(val context: Context) {

    companion object {
        private const val TAG = "SolidCRUD"
        @Volatile
        private lateinit var INSTANCE: SolidCRUD

        fun getInstance(context: Context): SolidCRUD {
            return if (::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                INSTANCE = SolidCRUD(context)
                INSTANCE
            }
        }
    }

    private lateinit var session: Session
    private lateinit var client: SolidSyncClient

    init {
        client = SolidSyncClient.getClient()
    }

    suspend fun getUserInfo(): UserInfo? {
        val isUserAuthenticated = checkAuthenticator()

        if (isUserAuthenticated){
            session = OpenIdSession.ofIdToken(Authenticator.getInstance(context).getAuthState().idToken)
            client = SolidSyncClient.getClient().session(session)

            val anotherSession = OpenIdSession.ofClientCredentials(
                URI("https://solidcommunity.net"),
                Authenticator.getInstance(context).getAuthState().lastRegistrationResponse!!.clientId,
                Authenticator.getInstance(context).getAuthState().clientSecret,
                "client_secret_basic"
            )

            delay(5000L)

            val xx = anotherSession.principal
            println("$xx")

            val userInfoReq: Request = Request.newBuilder()
                //.header(ACCEPT, "application/json")
                .uri(URI(Authenticator.getInstance(context).getAuthState().authorizationServiceConfiguration!!.discoveryDoc!!.userinfoEndpoint.toString()))
                .GET()
                .build()
            val userInfoResponse: Response<String> =
                client.send(userInfoReq, Response.BodyHandlers.ofString())
            return fromJsonStringToUserInfo(userInfoResponse.body())
        } else {
            return null
        }
    }

    private suspend fun checkAuthenticator(): Boolean {
        return if (Authenticator.getInstance(context).isUserAuthorized()) {
            if (!Authenticator.getInstance(context).needsTokenRefresh()) {
                true
            } else {
                //Need to refresh token
                val tokenRes = Authenticator.getInstance(context).refreshToken()
                Authenticator.getInstance(context).getAuthState().idToken != null
            }
        } else {
            //false
            val tokenRes = Authenticator.getInstance(context).refreshToken()
            Authenticator.getInstance(context).getAuthState().idToken != null
        }
    }

    fun test() {
        this.session = OpenIdSession.ofIdToken(Authenticator.getInstance(context).getAuthState().idToken)
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