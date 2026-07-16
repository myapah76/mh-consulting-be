package com.mhconsultingbe.servicecatalog.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminServiceCategoryResponse(
        UUID id,
        String slug,
        String name,
        boolean active,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
