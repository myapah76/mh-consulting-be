package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.entity.Admin;
import com.mhconsultingbe.auth.repository.AdminRepository;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class    AdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
    private final AdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    @Value("${app.admin.full-name:Administrator}")
    private String fullName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var normalizedEmail = TextNormalizer.lowercase(email);
        if (normalizedEmail == null || password == null || password.isBlank()) {
            log.warn("Initial admin was not created because ADMIN_EMAIL or ADMIN_PASSWORD is empty");
            return;
        }
        repository.findByEmailIgnoreCase(normalizedEmail).ifPresentOrElse(
                existing -> log.info("Initial admin already exists"),
                () -> {
                    var admin = new Admin();
                    admin.setEmail(normalizedEmail);
                    admin.setPasswordHash(passwordEncoder.encode(password));
                    admin.setFullName(TextNormalizer.trimToNull(fullName));
                    repository.save(admin);
                    log.info("Initial admin account created");
                });
    }
}
