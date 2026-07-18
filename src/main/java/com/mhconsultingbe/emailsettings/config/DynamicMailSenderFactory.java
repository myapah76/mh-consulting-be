package com.mhconsultingbe.emailsettings.config;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DynamicMailSenderFactory {
    private final EffectiveSmtpCredentialProvider credentialProvider;
    private final SmtpInfrastructureProperties infrastructure;

    public JavaMailSender create() {
        EffectiveSmtpCredentialProvider.EffectiveSmtpCredentials credentials =
                credentialProvider.getEffectiveCredentials();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(infrastructure.getHost());
        sender.setPort(infrastructure.getPort());
        sender.setUsername(credentials.username());
        sender.setPassword(credentials.password());

        var properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", Boolean.toString(infrastructure.isAuth()));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(infrastructure.isStartTls()));
        properties.put("mail.smtp.starttls.required", Boolean.toString(infrastructure.isStartTls()));
        properties.put("mail.smtp.ssl.enable", Boolean.toString(infrastructure.isImplicitSsl()));
        properties.put("mail.smtp.connectiontimeout", Integer.toString(infrastructure.getConnectionTimeout()));
        properties.put("mail.smtp.timeout", Integer.toString(infrastructure.getReadTimeout()));
        properties.put("mail.smtp.writetimeout", Integer.toString(infrastructure.getWriteTimeout()));
        return sender;
    }
}
