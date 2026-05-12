package com.erfangholami.androidsolidservices.api.auth.implementation

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Jwks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URI
import java.security.PublicKey

internal object IdTokenVerifier {

    private val httpClient = OkHttpClient()

    suspend fun verify(idToken: String, jwksUri: URI): Boolean {
        return try {
            val jwksJson = fetchJwks(jwksUri) ?: return false
            val kid = extractKid(idToken)
            val publicKey = resolveKey(jwksJson, kid) ?: return false
            Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(idToken)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchJwks(jwksUri: URI): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(jwksUri.toString()).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            JSONObject(body)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractKid(idToken: String): String? {
        return try {
            val header = idToken.split(".")[0]
            val decoded = android.util.Base64.decode(
                header,
                android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING,
            )
            JSONObject(String(decoded)).optString("kid").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveKey(jwksJson: JSONObject, kid: String?): PublicKey? {
        return try {
            val keys = jwksJson.getJSONArray("keys")
            for (i in 0..<keys.length()) {
                val keyJson = keys.getJSONObject(i)
                if (kid == null || keyJson.optString("kid") == kid) {
                    val jwk = Jwks.parser().build().parse(keyJson.toString())
                    return jwk.toKey() as? PublicKey
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
