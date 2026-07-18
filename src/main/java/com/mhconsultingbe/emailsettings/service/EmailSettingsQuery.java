package com.mhconsultingbe.emailsettings.service;

public interface EmailSettingsQuery {
    CurrentEmailSettings current();

    record CurrentEmailSettings(
            boolean enabled,
            String fromEmail,
            String fromName,
            String consultationRecipientEmail
    ) {
    }
}
