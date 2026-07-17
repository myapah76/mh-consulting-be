package com.mhconsultingbe.dashboard.dto.response;

import java.time.Instant;

public record DashboardPeriodResponse(
        Instant from,
        Instant to,
        String timezone
) {
}
