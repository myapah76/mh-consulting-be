package com.mhconsultingbe.pagecontent.dto;

import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.*;

public record PageSectionRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = ValidationPatterns.SLUG) String sectionKey,
        @Size(max = 300) String title,
        @Size(max = 500) String subtitle,
        @Size(max = 30000) String content,
        @Size(max = 2000) String imageUrl,
        @Size(max = 200) String buttonLabel,
        @Size(max = 2000) String buttonUrl,
        @PositiveOrZero Integer displayOrder,
        Boolean active
) {}
