package com.mhconsultingbe.consultation.dto;

import com.mhconsultingbe.consultation.entity.ConsultationStatus;
import java.time.Instant;
import java.util.UUID;

public record ConsultationResponse(UUID id, String customerName, String phone, String email, UUID serviceId,
                                   String serviceTitleSnapshot, String message, ConsultationStatus status,
                                   Instant createdAt, Instant updatedAt) {}
