package com.cleevio.vexl.common.utils;

public class NumberUtils {
    public static int parseIntOrFallback(final String valueToParse, final int fallback) {
        try {
            return Integer.parseInt(valueToParse);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
