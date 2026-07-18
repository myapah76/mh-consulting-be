package com.mhconsultingbe.emailsettings.mapper;

import com.mhconsultingbe.emailsettings.dto.response.EmailSettingsResponse;
import com.mhconsultingbe.emailsettings.entity.EmailSettings;

public final class EmailSettingsMapper {
    private static final String DELIVERY_PROVIDER = "SMTP";

    private EmailSettingsMapper() {
    }

    public static EmailSettingsResponse response(
            EmailSettings settings,
            boolean providerConfigured,
            String smtpUsername,
            boolean smtpPasswordConfigured
    ) {
        return new EmailSettingsResponse(
                settings.isEnabled(),
                settings.getFromEmail(),
                settings.getFromName(),
                settings.getConsultationRecipientEmail(),
                DELIVERY_PROVIDER,
                providerConfigured,
                smtpUsername,
                smtpPasswordConfigured
        );
    }
}
