package com.mhconsultingbe.auth.mail;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPasswordResetTemplateTests {
    @Test
    void rendersBrandingExpiryButtonAndVisibleFallbackUrl() {
        String resetUrl = "https://example.com/admin/reset-password?token=safe-raw-token";
        Context context = new Context(Locale.forLanguageTag("vi-VN"));
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expiresInMinutes", 30);
        context.setVariable("currentYear", 2026);

        String html = templateEngine().process("email/admin-password-reset", context);

        assertTrue(html.contains("MH Consulting"));
        assertTrue(html.contains("Đặt lại mật khẩu"));
        assertTrue(html.contains("30"));
        assertTrue(html.contains(resetUrl.replace("&", "&amp;")));
        assertTrue(html.contains("bỏ qua email này"));
        assertFalse(html.contains("SMTP_PASSWORD"));
        assertFalse(html.contains("password_hash"));
    }

    private TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
