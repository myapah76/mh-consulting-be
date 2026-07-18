package com.mhconsultingbe.emailsettings.exception;

public class EmailCredentialConfigurationException extends RuntimeException {
    public EmailCredentialConfigurationException(String message) {
        super(message);
    }

    public EmailCredentialConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
