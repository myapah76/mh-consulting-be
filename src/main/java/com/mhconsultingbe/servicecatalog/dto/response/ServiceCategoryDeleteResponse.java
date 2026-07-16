package com.mhconsultingbe.servicecatalog.dto.response;

public record ServiceCategoryDeleteResponse(
        boolean deleted,
        boolean active,
        String message
) {
}
