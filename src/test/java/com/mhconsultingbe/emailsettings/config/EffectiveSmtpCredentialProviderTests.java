package com.mhconsultingbe.emailsettings.config;

import com.mhconsultingbe.emailsettings.entity.EmailSettings;
import com.mhconsultingbe.emailsettings.exception.EmailCredentialConfigurationException;
import com.mhconsultingbe.emailsettings.repository.EmailSettingsRepository;
import com.mhconsultingbe.emailsettings.security.EmailCredentialCipher;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EffectiveSmtpCredentialProviderTests {
    private final EmailSettingsRepository repository = mock(EmailSettingsRepository.class);
    private final SmtpInfrastructureProperties infrastructure = infrastructure();
    private final EmailCredentialCipher cipher = new EmailCredentialCipher(
            Base64.getEncoder().encodeToString(new byte[32])
    );
    private final EffectiveSmtpCredentialProvider provider =
            new EffectiveSmtpCredentialProvider(repository, cipher, infrastructure);

    @Test
    void databaseCredentialsOverrideEnvironment() {
        EmailSettings settings = new EmailSettings();
        settings.setSmtpUsername("database@example.com");
        settings.setSmtpPasswordEncrypted(cipher.encrypt("database-password"));
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings));

        var credentials = provider.getEffectiveCredentials();

        assertEquals("database@example.com", credentials.username());
        assertEquals("database-password", credentials.password());
        assertEquals(EffectiveSmtpCredentialProvider.CredentialSource.DATABASE, credentials.source());
        assertTrue(provider.getStatus().passwordConfigured());
    }

    @Test
    void environmentIsUsedOnlyWhenDatabasePasswordIsAbsent() {
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(new EmailSettings()));

        var credentials = provider.getEffectiveCredentials();

        assertEquals("environment@example.com", credentials.username());
        assertEquals("environment-password", credentials.password());
        assertEquals(EffectiveSmtpCredentialProvider.CredentialSource.ENVIRONMENT, credentials.source());
    }

    @Test
    void malformedDatabaseCredentialDoesNotFallBack() {
        EmailSettings settings = new EmailSettings();
        settings.setSmtpUsername("database@example.com");
        settings.setSmtpPasswordEncrypted("v1:malformed:payload");
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings));

        assertThrows(
                EmailCredentialConfigurationException.class,
                provider::getEffectiveCredentials
        );
    }

    private SmtpInfrastructureProperties infrastructure() {
        SmtpInfrastructureProperties properties = new SmtpInfrastructureProperties();
        properties.setHost("smtp.example.com");
        properties.setEnvironmentUsername("environment@example.com");
        properties.setEnvironmentPassword("environment-password");
        return properties;
    }
}
