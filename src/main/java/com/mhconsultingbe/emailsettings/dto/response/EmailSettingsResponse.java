package com.mhconsultingbe.emailsettings.dto.response;

public record EmailSettingsResponse(
        boolean enabled,
        String fromEmail,
        String fromName,
        String consultationRecipientEmail,
        String deliveryProvider,
        boolean providerConfigured,
        String smtpUsername,
        boolean smtpPasswordConfigured
) {
}
