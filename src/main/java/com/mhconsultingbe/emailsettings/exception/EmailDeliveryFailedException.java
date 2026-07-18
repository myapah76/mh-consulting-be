package com.mhconsultingbe.emailsettings.exception;

public class EmailDeliveryFailedException extends RuntimeException {
    public EmailDeliveryFailedException(String message) {
        super(message);
    }
}
