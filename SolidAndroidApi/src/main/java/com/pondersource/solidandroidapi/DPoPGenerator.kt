package com.pondersource.solidandroidapi

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Jwks
import io.jsonwebtoken.security.SignatureAlgorithm
import net.openid.appauth.AuthorizationServiceDiscovery
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Date
import java.util.UUID
import javax.security.auth.x500.X500Principal


class DPoPGenerator private constructor(
    val authDiscovery: AuthorizationServiceDiscovery,
) {
    companion object {
        private val instances: MutableMap<String, DPoPGenerator> = mutableMapOf()

        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS_PREFIX = BuildConfig.KEY_GENERATOR_ALIAS

        fun getInstance(
            authDiscovery: AuthorizationServiceDiscovery,
        ): DPoPGenerator {
            val issuer = authDiscovery.issuer
            return synchronized(instances) {
                instances.getOrPut(issuer) {
                    DPoPGenerator(authDiscovery)
                }
            }
        }
    }

    private val selectedAlgo: DPopSupportedAlgo = selectCategory()

    @Volatile
    private var nonce: String? = null

    private val keyholder: KeyHolder = KeyPairHolderFactory.getKeyHolder(
        KEYSTORE_PROVIDER,
        "${KEYSTORE_ALIAS_PREFIX}_${selectedAlgo.name}",
        selectedAlgo
    )

    fun updateNonce(newNonce: String) {
        nonce = newNonce
    }

    fun generateProof(
        httpMethod: String,
        httpUri: String,
        accessToken: String? = null
    ): String {
        val headers: MutableMap<String, Any> = mutableMapOf()
        headers["typ"] = "dpop+jwt"
        headers["alg"] = keyholder.getAlgorithmName()
        headers["jwk"] = Jwks.builder().key<PublicKey, PrivateKey>(keyholder.getPublicKey()).build()

        val claims: MutableMap<String, Any> = mutableMapOf()
        claims["jti"] = UUID.randomUUID().toString()
        claims["htm"] = httpMethod
        claims["htu"] = httpUri
        claims["iat"] = Date().time / 1000
        nonce?.let { claims["nonce"] = it }
        if (!accessToken.isNullOrEmpty()) {
            claims["ath"] = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(accessToken.toByteArray(Charsets.US_ASCII))
            )
        }

        return Jwts.builder()
            .header().add(headers).and()
            .claims().add(claims).and()
            .signWith(keyholder.getPrivateKey(), keyholder.getAlgorithm())
            .compact()
    }

    private fun selectCategory(): DPopSupportedAlgo {
        if(!authDiscovery.supportsDPop() || authDiscovery.supportedDPopAlgorithms().isEmpty()) {
            throw IllegalArgumentException("Configuration doesn't support DPoP")
        }

        val algos = authDiscovery.supportedDPopAlgorithms()
        return if(algos.contains(DPopSupportedAlgo.ES256.toString())) {
            DPopSupportedAlgo.ES256
        } else if(algos.contains(DPopSupportedAlgo.ES384.toString())){
            DPopSupportedAlgo.ES384
        } else if(algos.contains(DPopSupportedAlgo.ES512.toString())){
            DPopSupportedAlgo.ES512
        } else if(algos.contains(DPopSupportedAlgo.RS256.toString())) {
            DPopSupportedAlgo.RS256
        } else if(algos.contains(DPopSupportedAlgo.RS384.toString())){
            DPopSupportedAlgo.RS384
        } else if(algos.contains(DPopSupportedAlgo.RS512.toString())){
            DPopSupportedAlgo.RS512
        } else {
            throw IllegalArgumentException("Configuration doesn't have supported algorithms.")
        }
    }

}

private enum class DPopSupportedAlgo{
    RS256,
    RS384,
    RS512,
    ES256,
    ES384,
    ES512,
}

private abstract class KeyHolder {
    abstract val provider: String
    abstract val alias: String
    abstract val keyPair: KeyPair

    fun getPublicKey(): PublicKey {
        return keyPair.public
    }
    fun getPrivateKey(): PrivateKey {
        return keyPair.private
    }
    fun getAlgorithmName(): String {
        return getAlgorithm().id
    }
    abstract fun getAlgorithm(): SignatureAlgorithm
}

private object KeyPairHolderFactory {
    fun getKeyHolder(provider: String, alias: String, algorithmName: String): KeyHolder {
        return getKeyHolder(provider, alias, DPopSupportedAlgo.valueOf(algorithmName))
    }

    fun getKeyHolder(provider: String, alias: String, algorithm: DPopSupportedAlgo): KeyHolder {
        return when(algorithm) {
            DPopSupportedAlgo.RS256 -> {
                RS256KeyHolder(provider, alias)
            }
            DPopSupportedAlgo.RS384 -> {
                RS384KeyHolder(provider, alias)
            }
            DPopSupportedAlgo.RS512 -> {
                RS512KeyHolder(provider, alias)
            }
            DPopSupportedAlgo.ES256 -> {
                ES256KeyHolder(provider, alias)
            }
            DPopSupportedAlgo.ES384 -> {
                ES384KeyHolder(provider, alias)
            }
            DPopSupportedAlgo.ES512 -> {
                ES512KeyHolder(provider, alias)
            }
        }
    }
}

private abstract class RSKeyHolder(
    override val provider: String,
    override val alias: String,
): KeyHolder() {

    private val localKeyPair: KeyPair

    abstract fun getDigest(): String

    override val keyPair: KeyPair
        get() = localKeyPair

    init {
        val keyStore = KeyStore.getInstance(provider)
        keyStore.load(null)

        if(keyStore.containsAlias(alias)) {
            val entry: KeyStore.Entry = keyStore.getEntry(alias, null)
            val privateKeyEntry: KeyStore.PrivateKeyEntry = (entry as KeyStore.PrivateKeyEntry)
            val privateKey: PrivateKey = privateKeyEntry.privateKey
            val publicKey: PublicKey = privateKeyEntry.certificate.publicKey
            localKeyPair = KeyPair(publicKey, privateKey)
        } else {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                provider
            )
            kpg.initialize(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                ).run {
                    setCertificateSerialNumber(BigInteger.valueOf(777))
                    setCertificateSubject(X500Principal("CN=$alias"))
                    setDigests(getDigest())
                    setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    setKeySize(2048)
                    build()
                }
            )
            localKeyPair = kpg.generateKeyPair()
        }
    }

}

private abstract class ESKeyHolder(
    override val provider: String,
    override val alias: String,
): KeyHolder() {

    private val localKeyPair: KeyPair

    override val keyPair: KeyPair
        get() = localKeyPair

    init {
        val keyStore = KeyStore.getInstance(provider)
        keyStore.load(null)

        if(keyStore.containsAlias(alias)) {
            val entry: KeyStore.Entry = keyStore.getEntry(alias, null)
            val privateKeyEntry: KeyStore.PrivateKeyEntry = (entry as KeyStore.PrivateKeyEntry)
            val privateKey: PrivateKey = privateKeyEntry.privateKey
            val publicKey: PublicKey = privateKeyEntry.certificate.publicKey
            localKeyPair = KeyPair(publicKey, privateKey)
        } else {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                provider
            )
            val spec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                build()
            }
            kpg.initialize(spec)
            localKeyPair = kpg.generateKeyPair()
        }
    }
}


private class RS256KeyHolder(
    provider: String,
    alias: String,
): RSKeyHolder(provider, alias) {

    override fun getAlgorithm(): SignatureAlgorithm {
        return Jwts.SIG.RS256
    }

    override fun getDigest(): String {
        return KeyProperties.DIGEST_SHA256
    }
}

private class RS384KeyHolder(
    provider: String,
    alias: String,
): RSKeyHolder(provider, alias) {

    override fun getAlgorithm(): SignatureAlgorithm {
        return Jwts.SIG.RS384
    }

    override fun getDigest(): String {
        return KeyProperties.DIGEST_SHA384
    }
}

private class RS512KeyHolder(
    provider: String,
    alias: String,
): RSKeyHolder(provider, alias) {

    override fun getAlgorithm(): SignatureAlgorithm {
        return Jwts.SIG.RS512
    }

    override fun getDigest(): String {
        return KeyProperties.DIGEST_SHA512
    }
}

private class ES256KeyHolder(
    provider: String,
    alias: String,
): ESKeyHolder(provider, alias) {
    override fun getAlgorithm(): SignatureAlgorithm {
        return Jwts.SIG.ES256
    }
}

private class ES384KeyHolder(
    provider: String,
    alias: String,
): ESKeyHolder(provider, alias) {
    override fun getAlgorithm(): SignatureAlgorithm {
        return Jwts.SIG.ES384
    }
}

private class ES512KeyHolder(
    provider: String,
    alias: String,
): ESKeyHolder(provider, alias) {
    override fun getAlgorithm(): SignatureAlgorithm {
        return Jwts.SIG.ES512
    }
}