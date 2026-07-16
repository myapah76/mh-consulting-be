package com.mhconsultingbe.consultation.mail;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultationNotificationTemplateTests {

    @Test
    void rendersCurrentFormFieldsAndDerivedCategory() {
        ConsultationSubmittedEvent event = new ConsultationSubmittedEvent(
                UUID.randomUUID(),
                "Nguyễn Văn A",
                "0912345678",
                "email@example.com",
                "Kế toán",
                "Dịch Vụ Kế Toán Trọn Gói",
                "Nội dung cần tư vấn",
                Instant.parse("2026-07-16T00:00:00Z")
        );
        Context context = new Context(Locale.forLanguageTag("vi-VN"));
        context.setVariable("request", event);
        context.setVariable("submittedAt", "16/07/2026 07:00:00");

        String html = templateEngine().process("email/consultation-notification", context);

        assertTrue(html.contains("Nguyễn Văn A"));
        assertTrue(html.contains("0912345678"));
        assertTrue(html.contains("email@example.com"));
        assertTrue(html.contains("Danh mục dịch vụ"));
        assertTrue(html.contains("Kế toán"));
        assertTrue(html.contains("Dịch Vụ Kế Toán Trọn Gói"));
        assertTrue(html.contains("Nội dung cần tư vấn"));
    }

    private TemplateEngine templateEngine() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
