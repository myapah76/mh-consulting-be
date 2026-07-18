package com.mhconsultingbe.auth.mail;

import com.mhconsultingbe.emailsettings.config.MailProviderConfiguration;
import com.mhconsultingbe.emailsettings.service.EmailSettingsQuery;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Year;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminPasswordResetMailService {
    private static final Logger log = LoggerFactory.getLogger(AdminPasswordResetMailService.class);
    private static final String SUBJECT = "MH Consulting - Đặt lại mật khẩu quản trị";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailSettingsQuery emailSettingsQuery;
    private final MailProviderConfiguration providerConfiguration;
    private final Clock clock;

    public void send(String recipientEmail, String resetUrl, int expiresInMinutes) {
        try {
            EmailSettingsQuery.CurrentEmailSettings settings = emailSettingsQuery.current();
            if (!settings.enabled() || !providerConfiguration.isConfigured()) {
                log.warn("Admin password reset email delivery is unavailable");
                return;
            }

            Context context = new Context(Locale.forLanguageTag("vi-VN"));
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("expiresInMinutes", expiresInMinutes);
            context.setVariable("currentYear", Year.now(clock).getValue());
            String html = templateEngine.process("email/admin-password-reset", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(settings.fromEmail(), settings.fromName());
            helper.setTo(recipientEmail);
            helper.setSubject(SUBJECT);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error(
                    "Admin password reset email delivery failed with {}",
                    exception.getClass().getSimpleName()
            );
        }
    }
}
