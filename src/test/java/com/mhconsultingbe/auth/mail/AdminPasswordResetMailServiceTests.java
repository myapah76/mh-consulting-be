package com.mhconsultingbe.auth.mail;

import com.mhconsultingbe.emailsettings.config.DynamicMailSenderFactory;
import com.mhconsultingbe.emailsettings.config.MailProviderConfiguration;
import com.mhconsultingbe.emailsettings.service.EmailSettingsQuery;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminPasswordResetMailServiceTests {
    @Test
    void passwordResetUsesFreshDynamicSender() {
        JavaMailSender sender = mock(JavaMailSender.class);
        DynamicMailSenderFactory factory = mock(DynamicMailSenderFactory.class);
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        EmailSettingsQuery settingsQuery = mock(EmailSettingsQuery.class);
        MailProviderConfiguration providerConfiguration = mock(MailProviderConfiguration.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(settingsQuery.current()).thenReturn(new EmailSettingsQuery.CurrentEmailSettings(
                true,
                "from@example.com",
                "MH Consulting",
                "consultations@example.com"
        ));
        when(providerConfiguration.isConfigured()).thenReturn(true);
        when(factory.create()).thenReturn(sender);
        when(sender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(anyString(), any())).thenReturn("<p>Reset</p>");
        AdminPasswordResetMailService service = new AdminPasswordResetMailService(
                factory,
                templateEngine,
                settingsQuery,
                providerConfiguration,
                Clock.fixed(Instant.parse("2026-07-18T00:00:00Z"), ZoneOffset.UTC)
        );

        service.send("admin@example.com", "https://example.com/reset?token=safe", 30);

        verify(factory).create();
        verify(sender).send(message);
    }
}
