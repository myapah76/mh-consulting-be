package com.mhconsultingbe.emailsettings.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailProviderConfiguration {
    private final SmtpInfrastructureProperties infrastructure;
    private final EffectiveSmtpCredentialProvider credentialProvider;

    public boolean isConfigured() {
        EffectiveSmtpCredentialProvider.EffectiveSmtpCredentials credentials =
                credentialProvider.getEffectiveCredentials();
        return hasText(infrastructure.getHost())
                && infrastructure.getPort() > 0
                && infrastructure.getPort() <= 65535
                && hasText(credentials.username())
                && hasText(credentials.password());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
