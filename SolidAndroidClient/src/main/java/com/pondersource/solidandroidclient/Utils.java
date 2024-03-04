package com.pondersource.solidandroidclient;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.inrupt.client.openid.OpenIdException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

import java.security.MessageDigest;

public class Utils {

    public static String getSessionIdentifier(final JwtClaims claims) {
        final String webid = claims.getClaimValueAsString("webid");
        if (webid != null) {
            return sha256(webid);
        }
        try {
            return sha256(String.join("|", claims.getIssuer(), claims.getSubject()));
        } catch (final MalformedClaimException ex) {
            // This exception will never occur because of the validation rules in parseIdToken
            throw new OpenIdException("Malformed ID Token: unable to extract issuer and subject", ex);
        }
    }

    public static String sha256(final String value) {
        final MessageDigest md = DigestUtils.getDigest("SHA-256");
        return new String(Hex.encodeHex(md.digest(value.getBytes(UTF_8))));
    }
}
