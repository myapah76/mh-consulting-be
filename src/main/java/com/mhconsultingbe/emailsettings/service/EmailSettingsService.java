package com.mhconsultingbe.emailsettings.service;

import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.emailsettings.config.DynamicMailSenderFactory;
import com.mhconsultingbe.emailsettings.config.EffectiveSmtpCredentialProvider;
import com.mhconsultingbe.emailsettings.config.MailProviderConfiguration;
import com.mhconsultingbe.emailsettings.config.SmtpInfrastructureProperties;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.dto.request.TestEmailRequest;
import com.mhconsultingbe.emailsettings.dto.response.EmailSettingsResponse;
import com.mhconsultingbe.emailsettings.entity.EmailSettings;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryFailedException;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryUnavailableException;
import com.mhconsultingbe.emailsettings.exception.EmailSettingsValidationException;
import com.mhconsultingbe.emailsettings.mapper.EmailSettingsMapper;
import com.mhconsultingbe.emailsettings.repository.EmailSettingsRepository;
import com.mhconsultingbe.emailsettings.security.EmailCredentialCipher;
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
    private final DynamicMailSenderFactory mailSenderFactory;
    private final EffectiveSmtpCredentialProvider credentialProvider;
    private final EmailCredentialCipher credentialCipher;
    private final SmtpInfrastructureProperties infrastructure;

    @Override
    @Transactional(readOnly = true)
    public EmailSettingsResponse get() {
        return response(required());
    }

    @Override
    @Transactional
    public EmailSettingsResponse update(EmailSettingsUpdateRequest request) {
        EmailSettings settings = required();
        String normalizedUsername = TextNormalizer.lowercase(request.smtpUsername());
        String currentUsername = currentEffectiveUsername(settings);
        boolean usernameChanged = !normalizedUsername.equals(currentUsername);
        boolean passwordProvided = request.smtpPassword() != null && !request.smtpPassword().isBlank();
        if (usernameChanged && !passwordProvided) {
            throw new EmailSettingsValidationException(
                    "smtpPassword",
                    "SMTP password is required when SMTP username changes"
            );
        }
        if (!passwordProvided && !credentialProvider.hasEffectivePassword()) {
            throw new EmailSettingsValidationException(
                    "smtpPassword",
                    "SMTP password is required when no SMTP password is configured"
            );
        }

        settings.setEnabled(request.enabled());
        settings.setFromEmail(TextNormalizer.lowercase(request.fromEmail()));
        settings.setFromName(TextNormalizer.plainText(request.fromName()));
        settings.setConsultationRecipientEmail(
                TextNormalizer.lowercase(request.consultationRecipientEmail())
        );
        settings.setSmtpUsername(normalizedUsername);
        if (passwordProvided) {
            settings.setSmtpPasswordEncrypted(
                    credentialCipher.encrypt(normalizePassword(request.smtpPassword()))
            );
        }
        return response(repository.save(settings));
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
            JavaMailSender mailSender = mailSenderFactory.create();
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

    private EmailSettingsResponse response(EmailSettings settings) {
        EffectiveSmtpCredentialProvider.CredentialStatus status = credentialProvider.getStatus();
        return EmailSettingsMapper.response(
                settings,
                providerConfiguration.isConfigured(),
                status.username(),
                status.passwordConfigured()
        );
    }

    private String currentEffectiveUsername(EmailSettings settings) {
        if (settings.getSmtpPasswordEncrypted() != null
                && !settings.getSmtpPasswordEncrypted().isBlank()
                && settings.getSmtpUsername() != null) {
            return settings.getSmtpUsername();
        }
        return credentialProvider.getEffectiveCredentials().username();
    }

    private String normalizePassword(String password) {
        if ("smtp.gmail.com".equalsIgnoreCase(infrastructure.getHost())) {
            String compact = password.replace(" ", "");
            if (compact.length() == 16 && compact.matches("[A-Za-z0-9]{16}")) {
                return compact;
            }
        }
        return password;
    }
}
