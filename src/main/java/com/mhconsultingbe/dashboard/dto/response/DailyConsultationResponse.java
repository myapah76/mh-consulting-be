package com.mhconsultingbe.dashboard.dto.response;

import java.time.LocalDate;

public record DailyConsultationResponse(
        LocalDate date,
        long count
) {
}
