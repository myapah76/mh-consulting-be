package com.mhconsultingbe.consultation.mail;

import com.mhconsultingbe.emailsettings.service.EmailSettingsQuery;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultationMailServiceTests {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final TemplateEngine templateEngine = mock(TemplateEngine.class);
    private final EmailSettingsQuery settingsQuery = mock(EmailSettingsQuery.class);
    private final ConsultationMailService service = new ConsultationMailService(
            mailSender,
            templateEngine,
            settingsQuery
    );

    @Test
    void disabledSettingsSkipNotification() {
        when(settingsQuery.current()).thenReturn(settings(false, "from@example.com", "recipient@example.com"));

        service.notifyCompany(event());

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enabledSettingsUseDatabaseSenderAndRecipient() throws Exception {
        MimeMessage message = message();
        when(settingsQuery.current()).thenReturn(settings(true, "from@example.com", "recipient@example.com"));
        when(templateEngine.process(anyString(), any())).thenReturn("<p>Consultation</p>");
        when(mailSender.createMimeMessage()).thenReturn(message);

        service.notifyCompany(event());

        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertEquals("from@example.com", from.getAddress());
        assertEquals("Sender Name", from.getPersonal());
        assertEquals("recipient@example.com", message.getRecipients(Message.RecipientType.TO)[0].toString());
        verify(mailSender).send(message);
    }

    @Test
    void nextNotificationUsesUpdatedSettingsWithoutRestart() throws Exception {
        MimeMessage first = message();
        MimeMessage second = message();
        when(settingsQuery.current()).thenReturn(
                settings(true, "first@example.com", "first-recipient@example.com"),
                settings(true, "second@example.com", "second-recipient@example.com")
        );
        when(templateEngine.process(anyString(), any())).thenReturn("<p>Consultation</p>");
        when(mailSender.createMimeMessage()).thenReturn(first, second);

        service.notifyCompany(event());
        service.notifyCompany(event());

        assertEquals("first@example.com", ((InternetAddress) first.getFrom()[0]).getAddress());
        assertEquals("second@example.com", ((InternetAddress) second.getFrom()[0]).getAddress());
        verify(settingsQuery, times(2)).current();
    }

    @Test
    void mailFailureIsContainedAfterConsultationCommit() {
        MimeMessage message = message();
        when(settingsQuery.current()).thenReturn(settings(true, "from@example.com", "recipient@example.com"));
        when(templateEngine.process(anyString(), any())).thenReturn("<p>Consultation</p>");
        when(mailSender.createMimeMessage()).thenReturn(message);
        org.mockito.Mockito.doThrow(new MailSendException("failure"))
                .when(mailSender)
                .send(message);

        assertDoesNotThrow(() -> service.notifyCompany(event()));
    }

    private EmailSettingsQuery.CurrentEmailSettings settings(
            boolean enabled,
            String from,
            String recipient
    ) {
        return new EmailSettingsQuery.CurrentEmailSettings(
                enabled,
                from,
                "Sender Name",
                recipient
        );
    }

    private ConsultationSubmittedEvent event() {
        return new ConsultationSubmittedEvent(
                UUID.randomUUID(),
                "Customer",
                "0912345678",
                "customer@example.com",
                "Category",
                "Service",
                "Message",
                Instant.parse("2026-07-18T00:00:00Z")
        );
    }

    private MimeMessage message() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }
}
