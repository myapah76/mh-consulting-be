package com.mhconsultingbe.servicecatalog.dto;

import java.util.UUID;

public record ServiceCategoryResponse(
        UUID id,
        String slug,
        String name,
        boolean active,
        int displayOrder
) {
}
