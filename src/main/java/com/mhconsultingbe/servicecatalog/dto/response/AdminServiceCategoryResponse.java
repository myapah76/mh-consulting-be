package com.mhconsultingbe.servicecatalog.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AdminServiceCategoryResponse(
        UUID id,
        String slug,
        String name,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
