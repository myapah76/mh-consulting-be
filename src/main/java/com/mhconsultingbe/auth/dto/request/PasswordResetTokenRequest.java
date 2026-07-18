package com.mhconsultingbe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetTokenRequest(
        @NotBlank
        @Size(max = 500)
        String token
) {
}
