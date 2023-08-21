package it.vexl.common.constants;


public class ClientVersion {
    public static final String CLIENT_VERSION_HEADER = "client-version";
    public static final String NEXT_PREFIX = "next:";

    public static final int MIN_CLIENT_VERSION_THAT_UNDERSTANDS_CANCELING = 43;
    public static final int DO_NOT_SENT_SYSTEM_NOTIFICATION_FROM_THIS_VERSION_ON = 55;

    private static int parseIntOrFallback(final String valueToParse, final int fallback) {
        try {
            return Integer.parseInt(valueToParse);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
    public static String getHashWithPrefixBasedOnClientVersion(final String hash, final String clientVersion) {
        final int clientVersionNumber = ClientVersion.parseIntOrFallback(clientVersion, 0);
        return clientVersionNumber >= 29 ? ClientVersion.NEXT_PREFIX + hash : hash;
    }
}
