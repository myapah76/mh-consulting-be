package com.mhconsultingbe.servicecatalog.dto;

import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ServiceResponse(
        UUID id, String slug, String title, ServiceCategory category, String shortDesc, String icon,
        String fullContent, boolean active, int displayOrder, List<String> detailedPoints,
        List<String> benefits, List<String> processSteps, Instant createdAt, Instant updatedAt
) {}
