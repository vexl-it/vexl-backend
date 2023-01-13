package com.cleevio.vexl.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhoneUtils {

    private static final String PHONE_PATTERN = "^\\+(?:[0-9] ?){6,14}[0-9]$";

    public static String trimAndDeleteSpacesFromPhoneNumber(String phoneNumber) {
        return phoneNumber.trim().replace(" ", "");
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches(PHONE_PATTERN);
    }
}
