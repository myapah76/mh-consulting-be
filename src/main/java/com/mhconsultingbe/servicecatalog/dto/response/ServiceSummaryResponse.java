package com.mhconsultingbe.servicecatalog.dto.response;

import java.util.UUID;

public record ServiceSummaryResponse(
        UUID id,
        String slug,
        String title,
        String category,
        UUID categoryId,
        String categoryName,
        String shortDesc,
        String icon,
        boolean active
) {
}
