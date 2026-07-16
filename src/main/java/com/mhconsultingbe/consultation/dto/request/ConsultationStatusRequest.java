package com.mhconsultingbe.consultation.dto.request;

import com.mhconsultingbe.consultation.entity.ConsultationStatus;
import jakarta.validation.constraints.NotNull;

public record ConsultationStatusRequest(
        @NotNull
        ConsultationStatus status
) {
}
