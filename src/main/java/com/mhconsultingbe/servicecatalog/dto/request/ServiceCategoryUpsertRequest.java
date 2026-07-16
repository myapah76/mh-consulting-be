package com.mhconsultingbe.servicecatalog.dto.request;

import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ServiceCategoryUpsertRequest(
        @NotBlank
        @Size(max = 200)
        @Pattern(
                regexp = ValidationPatterns.SLUG,
                message = "must be a lowercase URL-safe slug"
        )
        String slug,
        @NotBlank
        @Size(max = 200)
        String name,
        Boolean active
) {
}
