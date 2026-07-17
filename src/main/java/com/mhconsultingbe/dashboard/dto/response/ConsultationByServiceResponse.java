package com.mhconsultingbe.dashboard.dto.response;

import java.util.UUID;

public record ConsultationByServiceResponse(
        UUID serviceId,
        String serviceTitle,
        long count
) {
}
