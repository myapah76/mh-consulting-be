package com.mhconsultingbe.emailsettings.service;

import com.mhconsultingbe.emailsettings.config.MailProviderConfiguration;
import com.mhconsultingbe.emailsettings.config.DynamicMailSenderFactory;
import com.mhconsultingbe.emailsettings.config.EffectiveSmtpCredentialProvider;
import com.mhconsultingbe.emailsettings.config.SmtpInfrastructureProperties;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.dto.request.TestEmailRequest;
import com.mhconsultingbe.emailsettings.entity.EmailSettings;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryFailedException;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryUnavailableException;
import com.mhconsultingbe.emailsettings.exception.EmailSettingsValidationException;
import com.mhconsultingbe.emailsettings.repository.EmailSettingsRepository;
import com.mhconsultingbe.emailsettings.security.EmailCredentialCipher;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.Properties;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailSettingsServiceTests {
    private final EmailSettingsRepository repository = mock(EmailSettingsRepository.class);
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final DynamicMailSenderFactory mailSenderFactory = mock(DynamicMailSenderFactory.class);
    private final SmtpInfrastructureProperties infrastructure = infrastructure();
    private final EmailCredentialCipher cipher = new EmailCredentialCipher(
            Base64.getEncoder().encodeToString(new byte[32])
    );
    private final EffectiveSmtpCredentialProvider credentialProvider =
            new EffectiveSmtpCredentialProvider(repository, cipher, infrastructure);
    private final MailProviderConfiguration providerConfiguration =
            new MailProviderConfiguration(infrastructure, credentialProvider);

    EmailSettingsServiceTests() {
        when(mailSenderFactory.create()).thenReturn(mailSender);
    }

    @Test
    void retrievalDerivesProviderStatusWithoutExposingCredentials() {
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings(true)));

        var configured = service().get();
        infrastructure.setHost("");
        var unconfigured = service().get();

        assertTrue(configured.providerConfigured());
        assertFalse(unconfigured.providerConfigured());
        assertEquals("SMTP", configured.deliveryProvider());
        assertEquals("from@example.com", configured.fromEmail());
    }

    @Test
    void updateNormalizesEmailsAndPersistsExistingSingleton() {
        EmailSettings existing = settings(true);
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(existing));
        when(repository.save(any(EmailSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service().update(new EmailSettingsUpdateRequest(
                false,
                " FROM@EXAMPLE.COM ",
                " Updated Sender ",
                " RECIPIENT@EXAMPLE.COM ",
                "smtp-user@example.com",
                null
        ));

        verify(repository).save(existing);
        assertFalse(response.enabled());
        assertEquals("from@example.com", response.fromEmail());
        assertEquals("Updated Sender", response.fromName());
        assertEquals("recipient@example.com", response.consultationRecipientEmail());
    }

    @Test
    void newPasswordIsEncryptedAndGmailSpacesAreNormalized() {
        EmailSettings existing = settings(true);
        infrastructure.setHost("smtp.gmail.com");
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(existing));
        when(repository.save(any(EmailSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service().update(new EmailSettingsUpdateRequest(
                true,
                "from@example.com",
                "MH Consulting",
                "recipient@example.com",
                "new-user@example.com",
                "abcd efgh ijkl mnop"
        ));

        assertEquals("abcdefghijklmnop", cipher.decrypt(existing.getSmtpPasswordEncrypted()));
        assertFalse(existing.getSmtpPasswordEncrypted().contains("abcdefghijklmnop"));
        assertTrue(response.smtpPasswordConfigured());
    }

    @Test
    void blankPasswordRetainsExistingCiphertext() {
        EmailSettings existing = settings(true);
        existing.setSmtpUsername("database@example.com");
        String ciphertext = cipher.encrypt("existing-password");
        existing.setSmtpPasswordEncrypted(ciphertext);
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(existing));
        when(repository.save(any(EmailSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service().update(new EmailSettingsUpdateRequest(
                true,
                "from@example.com",
                "MH Consulting",
                "recipient@example.com",
                "database@example.com",
                " "
        ));

        assertEquals(ciphertext, existing.getSmtpPasswordEncrypted());
    }

    @Test
    void usernameChangeRequiresPasswordFieldError() {
        EmailSettings existing = settings(true);
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(existing));

        EmailSettingsValidationException exception = assertThrows(
                EmailSettingsValidationException.class,
                () -> service().update(new EmailSettingsUpdateRequest(
                        true,
                        "from@example.com",
                        "MH Consulting",
                        "recipient@example.com",
                        "changed@example.com",
                        null
                ))
        );

        assertEquals(
                "SMTP password is required when SMTP username changes",
                exception.getFieldErrors().get("smtpPassword")
        );
        verify(repository, never()).save(any(EmailSettings.class));
    }

    @Test
    void successfulTestUsesCurrentSenderAndRequestedRecipient() throws Exception {
        EmailSettings settings = settings(true);
        MimeMessage message = message();
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings));
        when(mailSender.createMimeMessage()).thenReturn(message);

        var response = service().sendTestEmail(
                new TestEmailRequest(" ADMIN@EXAMPLE.COM ")
        );

        assertEquals("Test email sent successfully", response.message());
        assertEquals("admin@example.com", message.getRecipients(Message.RecipientType.TO)[0].toString());
        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertEquals("from@example.com", from.getAddress());
        assertEquals("MH Consulting", from.getPersonal());
        verify(mailSender).send(message);
    }

    @Test
    void disabledOrUnconfiguredTestDeliveryIsRejectedBeforeSending() {
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings(false)));
        assertThrows(
                EmailDeliveryUnavailableException.class,
                () -> service().sendTestEmail(new TestEmailRequest("admin@example.com"))
        );

        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings(true)));
        assertThrows(
                EmailDeliveryUnavailableException.class,
                () -> {
                    infrastructure.setHost("");
                    service().sendTestEmail(new TestEmailRequest("admin@example.com"));
                }
        );
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void providerFailureBecomesSafeBusinessException() {
        MimeMessage message = message();
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings(true)));
        when(mailSender.createMimeMessage()).thenReturn(message);
        org.mockito.Mockito.doThrow(new MailSendException("provider detail"))
                .when(mailSender)
                .send(message);

        EmailDeliveryFailedException exception = assertThrows(
                EmailDeliveryFailedException.class,
                () -> service().sendTestEmail(new TestEmailRequest("admin@example.com"))
        );

        assertEquals("Unable to send test email", exception.getMessage());
    }

    private EmailSettingsService service() {
        return new EmailSettingsService(
                repository,
                providerConfiguration,
                mailSenderFactory,
                credentialProvider,
                cipher,
                infrastructure
        );
    }

    private EmailSettings settings(boolean enabled) {
        EmailSettings settings = new EmailSettings();
        settings.setEnabled(enabled);
        settings.setFromEmail("from@example.com");
        settings.setFromName("MH Consulting");
        settings.setConsultationRecipientEmail("consultations@example.com");
        return settings;
    }

    private SmtpInfrastructureProperties infrastructure() {
        SmtpInfrastructureProperties properties = new SmtpInfrastructureProperties();
        properties.setHost("smtp.example.com");
        properties.setEnvironmentUsername("smtp-user@example.com");
        properties.setEnvironmentPassword("smtp-secret");
        return properties;
    }

    private MimeMessage message() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }
}
