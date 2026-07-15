package com.mhconsultingbe.shared.util;

import com.mhconsultingbe.shared.exception.InvalidRequestException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class TextNormalizer {
    private static final Pattern HTML_TAG = Pattern.compile("<\\s*/?\\s*[a-zA-Z][^>]*>");
    private TextNormalizer() {}

    public static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String required(String value) {
        return value == null ? null : value.trim();
    }

    public static String lowercase(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    public static String plainText(String value) {
        String normalized = trimToNull(value);
        if (normalized != null && HTML_TAG.matcher(normalized).find()) {
            throw new InvalidRequestException("HTML is not accepted; use plain text or Markdown");
        }
        return normalized;
    }
}
