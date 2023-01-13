package com.cleevio.vexl.module.group.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeUtil {

    public static int generateQRCode() {
        SecureRandom rnd = new SecureRandom();
        return rnd.nextInt(999999);
    }
}
