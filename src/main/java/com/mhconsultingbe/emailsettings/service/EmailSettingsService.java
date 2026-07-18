package com.mhconsultingbe.emailsettings.service;

import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.emailsettings.config.MailProviderConfiguration;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.dto.request.TestEmailRequest;
import com.mhconsultingbe.emailsettings.dto.response.EmailSettingsResponse;
import com.mhconsultingbe.emailsettings.entity.EmailSettings;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryFailedException;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryUnavailableException;
import com.mhconsultingbe.emailsettings.mapper.EmailSettingsMapper;
import com.mhconsultingbe.emailsettings.repository.EmailSettingsRepository;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import com.mhconsultingbe.shared.util.TextNormalizer;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailSettingsService implements EmailSettingsOperations, EmailSettingsQuery {
    private static final Logger log = LoggerFactory.getLogger(EmailSettingsService.class);
    private static final String TEST_SUBJECT = "MH Consulting - Kiểm tra cấu hình email";
    private static final String TEST_BODY = "Email kiểm tra đã được gửi thành công từ hệ thống MH Consulting.";

    private final EmailSettingsRepository repository;
    private final MailProviderConfiguration providerConfiguration;
    private final JavaMailSender mailSender;

    @Override
    @Transactional(readOnly = true)
    public EmailSettingsResponse get() {
        return EmailSettingsMapper.response(
                required(),
                providerConfiguration.isConfigured()
        );
    }

    @Override
    @Transactional
    public EmailSettingsResponse update(EmailSettingsUpdateRequest request) {
        EmailSettings settings = required();
        settings.setEnabled(request.enabled());
        settings.setFromEmail(TextNormalizer.lowercase(request.fromEmail()));
        settings.setFromName(TextNormalizer.plainText(request.fromName()));
        settings.setConsultationRecipientEmail(
                TextNormalizer.lowercase(request.consultationRecipientEmail())
        );
        return EmailSettingsMapper.response(
                repository.save(settings),
                providerConfiguration.isConfigured()
        );
    }

    @Override
    public MessageResponse sendTestEmail(TestEmailRequest request) {
        CurrentEmailSettings settings = current();
        if (!settings.enabled()) {
            throw new EmailDeliveryUnavailableException("Email delivery is disabled");
        }
        if (!providerConfiguration.isConfigured()) {
            throw new EmailDeliveryUnavailableException("Email provider is not configured");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(settings.fromEmail(), settings.fromName());
            helper.setTo(TextNormalizer.lowercase(request.recipientEmail()));
            helper.setSubject(TEST_SUBJECT);
            helper.setText("<p>" + TEST_BODY + "</p>", true);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error(
                    "Test email delivery failed with {}",
                    exception.getClass().getSimpleName()
            );
            throw new EmailDeliveryFailedException("Unable to send test email");
        }

        return new MessageResponse("Test email sent successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentEmailSettings current() {
        EmailSettings settings = required();
        return new CurrentEmailSettings(
                settings.isEnabled(),
                settings.getFromEmail(),
                settings.getFromName(),
                settings.getConsultationRecipientEmail()
        );
    }

    private EmailSettings required() {
        return repository.findBySingletonKeyTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Email settings not configured"));
    }
}
