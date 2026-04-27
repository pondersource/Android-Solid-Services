package com.pondersource.shared.vocab

/**
 * WebID-TLS certificate vocabulary constants.
 * http://www.w3.org/ns/auth/cert#
 * Used in WebID profiles to advertise public keys for WebID-TLS authentication.
 */
object Cert {
    const val NAMESPACE = "http://www.w3.org/ns/auth/cert#"

    //Types
    const val KEY_TYPE = "${NAMESPACE}Key"
    const val RSA_PUBLIC_KEY = "${NAMESPACE}RSAPublicKey"
    const val CERTIFICATE = "${NAMESPACE}Certificate"

    //Predicates
    /** Links a WebID to a public key. */
    const val KEY = "${NAMESPACE}key"

    /** The RSA modulus (hex or integer). */
    const val MODULUS = "${NAMESPACE}modulus"

    /** The RSA public exponent. */
    const val EXPONENT = "${NAMESPACE}exponent"

    /** The label of a key. */
    const val LABEL = "${NAMESPACE}label"

    /** The identity the key belongs to. */
    const val IDENTITY = "${NAMESPACE}identity"
}
