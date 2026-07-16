package com.mhconsultingbe.consultation.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConsultationCreateRequest(
        @JsonAlias("fullName")
        @NotBlank
        @Size(max = 200)
        String customerName,
        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = ValidationPatterns.VIETNAMESE_PHONE,
                message = "Phone number is invalid"
        )
        String phone,
        @Email
        @Size(max = 320)
        String email,
        @NotNull
        UUID serviceId,
        @Size(max = 5000)
        String message
) {
}
