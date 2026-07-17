package com.mhconsultingbe.dashboard.dto.response;

import com.mhconsultingbe.consultation.entity.ConsultationStatus;

import java.time.Instant;
import java.util.UUID;

public record RecentConsultationResponse(
        UUID id,
        String customerName,
        String phone,
        String email,
        UUID serviceId,
        String serviceTitleSnapshot,
        ConsultationStatus status,
        Instant createdAt
) {
}
