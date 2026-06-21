package com.emailgenerator.util;
public final class LogMaskingUtil {

    private LogMaskingUtil() {
    }
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
