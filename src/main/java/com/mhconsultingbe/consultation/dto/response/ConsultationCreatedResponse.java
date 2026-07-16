package com.mhconsultingbe.consultation.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConsultationCreatedResponse(
        UUID id,
        String status,
        String message,
        Instant createdAt
) {
}
