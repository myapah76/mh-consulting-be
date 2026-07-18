package com.mhconsultingbe.emailsettings.config;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicMailSenderFactoryTests {
    @Test
    void eachCreateUsesCurrentCredentialsWithoutRestart() {
        EffectiveSmtpCredentialProvider provider = mock(EffectiveSmtpCredentialProvider.class);
        SmtpInfrastructureProperties infrastructure = new SmtpInfrastructureProperties();
        infrastructure.setHost("smtp.example.com");
        infrastructure.setPort(587);
        when(provider.getEffectiveCredentials()).thenReturn(
                credentials("first@example.com", "first-password"),
                credentials("second@example.com", "second-password")
        );
        DynamicMailSenderFactory factory = new DynamicMailSenderFactory(provider, infrastructure);

        JavaMailSenderImpl first = (JavaMailSenderImpl) factory.create();
        JavaMailSenderImpl second = (JavaMailSenderImpl) factory.create();

        assertNotSame(first, second);
        assertEquals("first@example.com", first.getUsername());
        assertEquals("first-password", first.getPassword());
        assertEquals("second@example.com", second.getUsername());
        assertEquals("second-password", second.getPassword());
    }

    private EffectiveSmtpCredentialProvider.EffectiveSmtpCredentials credentials(
            String username,
            String password
    ) {
        return new EffectiveSmtpCredentialProvider.EffectiveSmtpCredentials(
                username,
                password,
                EffectiveSmtpCredentialProvider.CredentialSource.DATABASE
        );
    }
}
