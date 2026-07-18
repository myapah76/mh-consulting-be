package com.mhconsultingbe.auth.dto.response;

import java.time.Instant;

public record PasswordResetTokenValidationResponse(
        boolean valid,
        Instant expiresAt
) {
}
