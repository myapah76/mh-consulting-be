package com.mhconsultingbe.emailsettings.exception;

import java.util.Map;

public class EmailSettingsValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public EmailSettingsValidationException(String field, String message) {
        super("Request validation failed");
        this.fieldErrors = Map.of(field, message);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
