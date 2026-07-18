package com.mhconsultingbe.consultation.mail;

import com.mhconsultingbe.emailsettings.service.EmailSettingsQuery;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ConsultationMailService {

    private static final Logger log =
            LoggerFactory.getLogger(ConsultationMailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailSettingsQuery emailSettingsQuery;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyCompany(ConsultationSubmittedEvent event) {
        try {
            EmailSettingsQuery.CurrentEmailSettings settings = emailSettingsQuery.current();
            if (!settings.enabled()) {
                log.info(
                        "Consultation email delivery is disabled; notification skipped for request {}",
                        event.id()
                );
                return;
            }

            var context = new Context(Locale.forLanguageTag("vi-VN"));
            context.setVariable("request", event);
            context.setVariable(
                    "submittedAt",
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                            .format(event.submittedAt())
            );

            String html = templateEngine.process(
                    "email/consultation-notification",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(settings.fromEmail(), settings.fromName());
            helper.setTo(settings.consultationRecipientEmail());

            if (event.email() != null && !event.email().isBlank()) {
                helper.setReplyTo(event.email());
            }

            helper.setSubject(
                    "Yêu cầu tư vấn mới - " + event.customerName()
            );
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception exception) {
            log.error(
                    "Failed to send consultation notification for request {} with {}",
                    event.id(),
                    exception.getClass().getSimpleName()
            );
        }
    }
}
