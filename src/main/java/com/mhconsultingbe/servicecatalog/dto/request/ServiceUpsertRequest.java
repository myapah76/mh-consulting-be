package com.mhconsultingbe.servicecatalog.dto.request;

import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ServiceUpsertRequest(
        @NotBlank
        @Size(max = 200)
        @Pattern(regexp = ValidationPatterns.SLUG, message = "must be a lowercase URL-safe slug")
        String slug,
        @NotBlank
        @Size(max = 200)
        String title,
        @NotNull
        UUID categoryId,
        @NotBlank
        @Size(max = 1000)
        String shortDesc,
        @Size(max = 100)
        String icon,
        @Size(max = 20000)
        String fullContent,
        Boolean active,
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
) {
}
