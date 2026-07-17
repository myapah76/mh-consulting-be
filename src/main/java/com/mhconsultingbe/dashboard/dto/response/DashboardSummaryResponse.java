package com.mhconsultingbe.dashboard.dto.response;

public record DashboardSummaryResponse(
        long total,
        long newCount,
        long contactedCount,
        long completedCount,
        long cancelledCount,
        double completionRate
) {
}
