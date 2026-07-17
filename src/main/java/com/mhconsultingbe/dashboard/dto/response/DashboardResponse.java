package com.mhconsultingbe.dashboard.dto.response;

import java.util.List;

public record DashboardResponse(
        DashboardPeriodResponse period,
        DashboardSummaryResponse summary,
        List<DailyConsultationResponse> dailyConsultations,
        List<ConsultationByServiceResponse> consultationsByService,
        List<ConsultationByCategoryResponse> consultationsByCategory,
        List<RecentConsultationResponse> recentConsultations
) {
}
