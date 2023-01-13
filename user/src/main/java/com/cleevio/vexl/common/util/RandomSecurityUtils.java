package com.cleevio.vexl.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.stream.IntStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomSecurityUtils {

    private static final String ALGORITHM = "SHA1PRNG";
    private static final String PROVIDER = "SUN";

    public static String retrieveRandomDigits(int length) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM, PROVIDER);
            final int seed = secureRandom.nextInt(10);
            return IntStream.iterate(seed, i -> secureRandom.nextInt(10))
                    .limit(length)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error("Error occurred during a generation of random digits. Error: {}", e.getMessage());
            throw new RuntimeException("Error occurred during a generation of random digits.");
        }
    }
}
