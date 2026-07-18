package com.mhconsultingbe.emailsettings.dto.request;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailSettingsRequestTests {
    @Test
    void updateRequiresAllFieldsAndValidEmails() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new EmailSettingsUpdateRequest(
                    null,
                    "invalid",
                    " ",
                    "also-invalid",
                    "not-an-email",
                    null
            );

            Set<String> fields = factory.getValidator().validate(request).stream()
                    .map(violation -> violation.getPropertyPath().toString())
                    .collect(Collectors.toSet());
            assertTrue(fields.containsAll(Set.of(
                    "enabled",
                    "fromEmail",
                    "fromName",
                    "consultationRecipientEmail",
                    "smtpUsername"
            )));
        }
    }

    @Test
    void testRecipientIsRequiredAndValidated() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(new TestEmailRequest(" ")).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("recipientEmail")));
            assertTrue(factory.getValidator().validate(new TestEmailRequest("invalid")).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("recipientEmail")));
        }
    }

    @Test
    void valuesAreTrimmedBeforeValidationAndUse() {
        var request = new EmailSettingsUpdateRequest(
                true,
                " FROM@EXAMPLE.COM ",
                " MH Consulting ",
                " RECIPIENT@EXAMPLE.COM ",
                " SMTP@EXAMPLE.COM ",
                " app password "
        );

        assertEquals("FROM@EXAMPLE.COM", request.fromEmail());
        assertEquals("MH Consulting", request.fromName());
        assertEquals("RECIPIENT@EXAMPLE.COM", request.consultationRecipientEmail());
        assertEquals("SMTP@EXAMPLE.COM", request.smtpUsername());
        assertEquals(" app password ", request.smtpPassword());
        assertEquals("admin@example.com", new TestEmailRequest(" admin@example.com ").recipientEmail());
    }
}
