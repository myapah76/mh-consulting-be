package com.mhconsultingbe.consultation.dto;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultationCreateRequestTests {

    @Test
    void requiresServiceId() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var request = new ConsultationCreateRequest(
                    "Nguyễn Văn A",
                    "0912345678",
                    "email@example.com",
                    null,
                    "Nội dung cần tư vấn"
            );

            var violations = validatorFactory.getValidator().validate(request);

            assertTrue(violations.stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("serviceId")));
        }
    }
}
