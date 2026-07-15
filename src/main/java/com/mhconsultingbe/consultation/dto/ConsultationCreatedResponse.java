package com.mhconsultingbe.consultation.dto;

import java.time.Instant;
import java.util.UUID;

public record ConsultationCreatedResponse(UUID id, String status, String message, Instant createdAt) {}
