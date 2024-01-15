package com.pondersource.androidsolidservices

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inrupt.client.Request
import com.inrupt.client.Response
import com.inrupt.client.openid.OpenIdSession
import com.inrupt.client.solid.SolidSyncClient
import com.inrupt.client.spi.HttpService
import com.inrupt.client.spi.ServiceProvider
import com.pondersource.androidsolidservices.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.http.HttpHeaders.CONTENT_TYPE
import java.io.PrintWriter
import java.net.URI


class MainActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO) {
            val session = OpenIdSession.ofClientCredentials(
                URI.create("https://login.inrupt.com").normalize(),
                "59c7fc44-3186-4200-a6d9-6de6a0bd0962",
                "8ea04e15-e7c0-4166-80e3-109d8805e8ed",
                "client_secret_basic"
            )

            Log.d(TAG, "Session is:" + session.id)

            val client = SolidSyncClient.getClient().session(session)
            val printWriter = PrintWriter(System.out, true)

            if (session != null && session.principal != null) {

                session.principal.ifPresent { webid: URI? ->
                    printWriter.format("WebID: %s", webid)
                    printWriter.println()

                    /*val myNonRDFFile = SolidNonRDFSource(
                        URI.create("https://storage.inrupt.com/4a1ea008-8f12-4451-8cce-1f6f0be6b2ce/hello.txt")
                            .normalize(),
                        "text/plain",
                        "Hello solid".byteInputStream() // to input stream
                    )
                    client.create(myNonRDFFile).getIdentifier().toString()
*/
                    val okClient: HttpService = ServiceProvider.getHttpService()
                    val request: Request = Request.newBuilder()
                        .uri(URI("https://storage.inrupt.com/4a1ea008-8f12-4451-8cce-1f6f0be6b2ce/hello.txt"))
                        .GET()
                        .build()
                    val response: Response<ByteArray> =
                        okClient.send(request, Response.BodyHandlers.ofByteArray())
                            .toCompletableFuture().join()

                    Log.d("MainActivity","HTTP status code: " + response.statusCode())
                    Log.d("MainActivity","Response uri: " + response.uri())
                    Log.d("MainActivity",
                        "Content type: " + response.headers().asMap().get(CONTENT_TYPE)
                    )
                }
            }
        }
    }
}