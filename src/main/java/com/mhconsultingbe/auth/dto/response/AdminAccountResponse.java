package com.mhconsultingbe.auth.dto.response;

import java.util.UUID;

public record AdminAccountResponse(
        UUID id,
        String email,
        String fullName,
        String role,
        boolean active
) {
}
