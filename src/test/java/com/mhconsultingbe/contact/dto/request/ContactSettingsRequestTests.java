package com.mhconsultingbe.contact.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactSettingsRequestTests {
    private static jakarta.validation.ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void rejectsEveryBlankRequiredField() {
        var violations = validator.validate(new ContactSettingsRequest(
                " ",
                " ",
                null,
                null,
                null,
                " ",
                " ",
                null,
                null,
                null
        ));

        Set<String> fields = violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(fields.containsAll(Set.of(
                "address",
                "primaryPhone",
                "email",
                "workingHours"
        )));
    }

    @Test
    void rejectsInvalidPhonesEmailAndUrls() {
        var request = validRequest(
                "invalid",
                "also-invalid",
                "not-an-email",
                "javascript:alert(1)",
                "data:text/plain,bad",
                "/relative"
        );
        Set<String> fields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(fields.containsAll(Set.of(
                "primaryPhone",
                "secondaryPhone",
                "email",
                "facebookUrl",
                "zaloUrl",
                "youtubeUrl"
        )));
    }

    @Test
    void rejectsExcessivelyLongValues() {
        var request = new ContactSettingsRequest(
                "a".repeat(1001),
                "0903.024.116",
                "l".repeat(101),
                "0938.835.633",
                "l".repeat(101),
                "a".repeat(310) + "@example.com",
                "h".repeat(256),
                "https://example.com/" + "a".repeat(1000),
                null,
                null
        );

        Set<String> fields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(fields.containsAll(Set.of(
                "address",
                "primaryPhoneLabel",
                "secondaryPhoneLabel",
                "email",
                "workingHours",
                "facebookUrl"
        )));
    }

    @Test
    void trimsInputAndConvertsBlankOptionalValuesToNull() {
        var request = new ContactSettingsRequest(
                " Address ",
                " 0903.024.116 ",
                " ",
                " ",
                " Label without phone ",
                " INFO@EXAMPLE.COM ",
                " Working hours ",
                " ",
                " https://zalo.me/0903024116 ",
                " "
        );

        assertEquals("Address", request.address());
        assertEquals("0903.024.116", request.primaryPhone());
        assertEquals("INFO@EXAMPLE.COM", request.email());
        assertEquals("https://zalo.me/0903024116", request.zaloUrl());
        assertEquals(null, request.primaryPhoneLabel());
        assertEquals(null, request.secondaryPhone());
        assertEquals(null, request.facebookUrl());
        assertEquals(null, request.youtubeUrl());
    }

    private ContactSettingsRequest validRequest(
            String primaryPhone,
            String secondaryPhone,
            String email,
            String facebookUrl,
            String zaloUrl,
            String youtubeUrl
    ) {
        return new ContactSettingsRequest(
                "Address",
                primaryPhone,
                "Primary",
                secondaryPhone,
                "Secondary",
                email,
                "Working hours",
                facebookUrl,
                zaloUrl,
                youtubeUrl
        );
    }
}
