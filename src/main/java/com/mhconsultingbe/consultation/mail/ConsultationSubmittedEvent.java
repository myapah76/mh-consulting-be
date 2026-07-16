package com.mhconsultingbe.consultation.mail;

import java.time.Instant;
import java.util.UUID;

public record ConsultationSubmittedEvent(UUID id, String customerName, String phone, String email,
                                         String serviceCategoryName, String serviceTitle, String message,
                                         Instant submittedAt) {}
