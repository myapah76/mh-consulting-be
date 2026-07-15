package com.mhconsultingbe.servicecatalog.dto;

import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import java.util.UUID;

public record ServiceSummaryResponse(UUID id, String slug, String title, ServiceCategory category,
                                     String shortDesc, String icon, boolean active, int displayOrder) {}
