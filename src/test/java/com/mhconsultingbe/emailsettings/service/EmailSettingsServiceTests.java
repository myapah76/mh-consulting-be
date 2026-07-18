package com.mhconsultingbe.emailsettings.service;

import com.mhconsultingbe.emailsettings.config.MailProviderConfiguration;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.dto.request.TestEmailRequest;
import com.mhconsultingbe.emailsettings.entity.EmailSettings;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryFailedException;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryUnavailableException;
import com.mhconsultingbe.emailsettings.repository.EmailSettingsRepository;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.Properties;

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

    @Test
    void retrievalDerivesProviderStatusWithoutExposingCredentials() {
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings(true)));

        var configured = service(provider(true)).get();
        var unconfigured = service(provider(false)).get();

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

        var response = service(provider(true)).update(new EmailSettingsUpdateRequest(
                false,
                " FROM@EXAMPLE.COM ",
                " Updated Sender ",
                " RECIPIENT@EXAMPLE.COM "
        ));

        verify(repository).save(existing);
        assertFalse(response.enabled());
        assertEquals("from@example.com", response.fromEmail());
        assertEquals("Updated Sender", response.fromName());
        assertEquals("recipient@example.com", response.consultationRecipientEmail());
    }

    @Test
    void successfulTestUsesCurrentSenderAndRequestedRecipient() throws Exception {
        EmailSettings settings = settings(true);
        MimeMessage message = message();
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings));
        when(mailSender.createMimeMessage()).thenReturn(message);

        var response = service(provider(true)).sendTestEmail(
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
                () -> service(provider(true)).sendTestEmail(new TestEmailRequest("admin@example.com"))
        );

        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(settings(true)));
        assertThrows(
                EmailDeliveryUnavailableException.class,
                () -> service(provider(false)).sendTestEmail(new TestEmailRequest("admin@example.com"))
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
                () -> service(provider(true)).sendTestEmail(new TestEmailRequest("admin@example.com"))
        );

        assertEquals("Unable to send test email", exception.getMessage());
    }

    private EmailSettingsService service(MailProviderConfiguration provider) {
        return new EmailSettingsService(repository, provider, mailSender);
    }

    private MailProviderConfiguration provider(boolean configured) {
        return configured
                ? new MailProviderConfiguration("smtp.example.com", "smtp-user", "smtp-secret")
                : new MailProviderConfiguration("", "", "");
    }

    private EmailSettings settings(boolean enabled) {
        EmailSettings settings = new EmailSettings();
        settings.setEnabled(enabled);
        settings.setFromEmail("from@example.com");
        settings.setFromName("MH Consulting");
        settings.setConsultationRecipientEmail("consultations@example.com");
        return settings;
    }

    private MimeMessage message() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }
}
