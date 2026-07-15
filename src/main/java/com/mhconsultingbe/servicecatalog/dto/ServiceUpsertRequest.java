package com.mhconsultingbe.servicecatalog.dto;

import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.*;

import java.util.List;

public record ServiceUpsertRequest(
        @NotBlank
        @Size(max = 200)
        @Pattern(regexp = ValidationPatterns.SLUG, message = "must be a lowercase URL-safe slug")
        String slug,
        @NotBlank
        @Size(max = 200)
        String title,
        @NotNull
        ServiceCategory category,
        @NotBlank
        @Size(max = 1000)
        String shortDesc,
        @Size(max = 100)
        String icon,
        @Size(max = 20000)
        String fullContent,
        Boolean active,
        @PositiveOrZero
        Integer displayOrder,
        @Size(max = 100)
        List<
                @NotBlank
                @Size(max = 2000)
                String> detailedPoints,
        @Size(max = 100)
        List<
                @NotBlank
                @Size(max = 2000)
                String> benefits,
        @Size(max = 100)
        List<
                @NotBlank
                @Size(max = 2000)
                String> processSteps
) {}
