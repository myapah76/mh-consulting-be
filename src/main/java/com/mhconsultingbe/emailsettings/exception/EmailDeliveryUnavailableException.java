package com.mhconsultingbe.emailsettings.exception;

public class EmailDeliveryUnavailableException extends RuntimeException {
    public EmailDeliveryUnavailableException(String message) {
        super(message);
    }
}
