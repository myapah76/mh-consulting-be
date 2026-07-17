package com.mhconsultingbe.dashboard.dto.response;

public record ConsultationByCategoryResponse(
        String categoryName,
        long count
) {
}
