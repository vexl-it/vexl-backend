package com.cleevio.vexl.module.user.util;

import com.cleevio.vexl.common.cryptolib.CryptoLibrary;
import com.cleevio.vexl.module.user.dto.UserData;
import com.cleevio.vexl.module.user.exception.VerificationExpiredException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating challenge and processing of verification of challenge.
 * <p>
 * Challenge is an important element in verifying that a user has a private key to his public key.
 * We create a random challenge for him, he has to sign it with his private key, and then we verify with his public key that it is indeed signed by him.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChallengeUtil {
    private static final String SHA256 = "SHA-256";

    public static String generateChallenge() throws NoSuchAlgorithmException {
        byte[] bytes = generateCodeVerifier().getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest = MessageDigest.getInstance(SHA256);
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    public static boolean isSignedChallengeValid(final UserData userData, final int cryptoVersion)
            throws VerificationExpiredException {

        if (cryptoVersion >= 2) {
            return CryptoLibrary.instance.ecdsaVerifyV2(
                    userData.publicKey(),
                    userData.challenge(),
                    userData.signature()
            );
        }

        return CryptoLibrary.instance.ecdsaVerifyV1(
                userData.publicKey(),
                userData.challenge(),
                userData.signature()
        );
    }
}
