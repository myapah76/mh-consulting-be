package com.mhconsultingbe.emailsettings.config;

import com.mhconsultingbe.emailsettings.entity.EmailSettings;
import com.mhconsultingbe.emailsettings.exception.EmailCredentialConfigurationException;
import com.mhconsultingbe.emailsettings.repository.EmailSettingsRepository;
import com.mhconsultingbe.emailsettings.security.EmailCredentialCipher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EffectiveSmtpCredentialProvider {
    private final EmailSettingsRepository repository;
    private final EmailCredentialCipher cipher;
    private final SmtpInfrastructureProperties infrastructure;

    public EffectiveSmtpCredentials getEffectiveCredentials() {
        EmailSettings settings = repository.findBySingletonKeyTrue().orElse(null);
        if (settings != null && hasText(settings.getSmtpPasswordEncrypted())) {
            if (!hasText(settings.getSmtpUsername())) {
                throw new EmailCredentialConfigurationException("Stored SMTP username is not configured");
            }
            String password = cipher.decrypt(settings.getSmtpPasswordEncrypted());
            if (!hasText(password)) {
                throw new EmailCredentialConfigurationException("Stored SMTP credential is invalid");
            }
            return new EffectiveSmtpCredentials(
                    settings.getSmtpUsername(),
                    password,
                    CredentialSource.DATABASE
            );
        }

        if (hasText(infrastructure.getEnvironmentUsername())
                && hasText(infrastructure.getEnvironmentPassword())) {
            return new EffectiveSmtpCredentials(
                    infrastructure.getEnvironmentUsername().trim().toLowerCase(java.util.Locale.ROOT),
                    infrastructure.getEnvironmentPassword(),
                    CredentialSource.ENVIRONMENT
            );
        }
        return new EffectiveSmtpCredentials("", "", CredentialSource.NONE);
    }

    public CredentialStatus getStatus() {
        EmailSettings settings = repository.findBySingletonKeyTrue().orElse(null);
        String dbUsername = settings == null ? null : settings.getSmtpUsername();
        String encryptedPassword = settings == null ? null : settings.getSmtpPasswordEncrypted();
        boolean databasePasswordConfigured = false;
        if (hasText(encryptedPassword)) {
            databasePasswordConfigured = hasText(cipher.decrypt(encryptedPassword));
        }
        boolean environmentPasswordConfigured = hasText(infrastructure.getEnvironmentPassword());
        String displayedUsername = hasText(dbUsername)
                ? dbUsername
                : valueOrEmpty(infrastructure.getEnvironmentUsername());
        return new CredentialStatus(
                displayedUsername,
                databasePasswordConfigured || environmentPasswordConfigured
        );
    }

    public boolean hasEffectivePassword() {
        return hasText(getEffectiveCredentials().password());
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record EffectiveSmtpCredentials(
            String username,
            String password,
            CredentialSource source
    ) {
    }

    public record CredentialStatus(
            String username,
            boolean passwordConfigured
    ) {
    }

    public enum CredentialSource {
        DATABASE,
        ENVIRONMENT,
        NONE
    }
}
