package com.emailgenerator.util;

/**
 * Utility for masking sensitive values in logs.
 */
public final class LogMaskingUtil {

    private LogMaskingUtil() {
    }

    /**
     * Masks an API key, revealing only the last four characters.
     *
     * @param apiKey the API key to mask
     * @return masked representation safe for logging
     */
    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[NOT SET]";
        }
        if (apiKey.length() <= 4) {
            return "****";
        }
        return "*".repeat(Math.min(apiKey.length() - 4, 12)) + apiKey.substring(apiKey.length() - 4);
    }
}
