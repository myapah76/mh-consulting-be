package com.mhconsultingbe.emailsettings.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailProviderConfiguration {
    private final String host;
    private final String username;
    private final String password;

    public MailProviderConfiguration(
            @Value("${spring.mail.host:}")
            String host,
            @Value("${spring.mail.username:}")
            String username,
            @Value("${spring.mail.password:}")
            String password
    ) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public boolean isConfigured() {
        return hasText(host)
                && hasText(username)
                && hasText(password);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
