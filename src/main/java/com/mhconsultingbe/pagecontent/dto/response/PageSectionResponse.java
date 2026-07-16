package com.mhconsultingbe.pagecontent.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PageSectionResponse(
        UUID id,
        String pageKey,
        String sectionKey,
        String title,
        String subtitle,
        String content,
        String imageUrl,
        String buttonLabel,
        String buttonUrl,
        int displayOrder,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
