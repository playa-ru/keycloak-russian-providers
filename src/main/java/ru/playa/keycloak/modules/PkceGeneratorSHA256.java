package ru.playa.keycloak.modules;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.Charset;

public class PkceGeneratorSHA256 {

    public String generateRandomCodeVerifier(SecureRandom entropySource) {
        byte[] randomBytes = new byte[MIN_CODE_VERIFIER_ENTROPY];
        entropySource.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String deriveCodeVerifierChallenge(String codeVerifier) {
        try {
            MessageDigest sha256Digester = MessageDigest.getInstance(ALGORITHM);
            byte[] input = codeVerifier.getBytes(Charset.forName(CHARSET_NAME));
            sha256Digester.update(input);
            byte[] digestBytes = sha256Digester.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digestBytes);
        } catch (NoSuchAlgorithmException nsae) {
            return codeVerifier;
        }
    }

    private static final String ALGORITHM = "SHA-256";
    private static final String CHARSET_NAME = "ISO-8859-1";
    private static final int MIN_CODE_VERIFIER_ENTROPY = 128;
}
