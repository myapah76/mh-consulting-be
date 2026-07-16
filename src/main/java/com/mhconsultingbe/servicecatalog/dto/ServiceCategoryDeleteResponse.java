package com.mhconsultingbe.servicecatalog.dto;

public record ServiceCategoryDeleteResponse(
        boolean deleted,
        boolean active,
        String message
) {
}
