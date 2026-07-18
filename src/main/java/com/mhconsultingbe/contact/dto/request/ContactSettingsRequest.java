package com.mhconsultingbe.contact.dto.request;

import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ContactSettingsRequest(
        @NotBlank
        @Size(max = 1000)
        String address,
        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = ValidationPatterns.VIETNAMESE_PHONE,
                message = "Phone number is invalid"
        )
        String primaryPhone,
        @Size(max = 100)
        String primaryPhoneLabel,
        @Size(max = 50)
        @Pattern(
                regexp = ValidationPatterns.VIETNAMESE_PHONE,
                message = "Phone number is invalid"
        )
        String secondaryPhone,
        @Size(max = 100)
        String secondaryPhoneLabel,
        @NotBlank
        @Email
        @Size(max = 320)
        String email,
        @NotBlank
        @Size(max = 255)
        String workingHours,
        @Size(max = 1000)
        @Pattern(
                regexp = ValidationPatterns.ABSOLUTE_HTTP_URL,
                message = "must be an absolute HTTP or HTTPS URL"
        )
        @URL(message = "must be a valid URL")
        String facebookUrl,
        @Size(max = 1000)
        @Pattern(
                regexp = ValidationPatterns.ABSOLUTE_HTTP_URL,
                message = "must be an absolute HTTP or HTTPS URL"
        )
        @URL(message = "must be a valid URL")
        String zaloUrl,
        @Size(max = 1000)
        @Pattern(
                regexp = ValidationPatterns.ABSOLUTE_HTTP_URL,
                message = "must be an absolute HTTP or HTTPS URL"
        )
        @URL(message = "must be a valid URL")
        String youtubeUrl
) {
    public ContactSettingsRequest {
        address = trimToNull(address);
        primaryPhone = trimToNull(primaryPhone);
        primaryPhoneLabel = trimToNull(primaryPhoneLabel);
        secondaryPhone = trimToNull(secondaryPhone);
        secondaryPhoneLabel = trimToNull(secondaryPhoneLabel);
        email = trimToNull(email);
        workingHours = trimToNull(workingHours);
        facebookUrl = trimToNull(facebookUrl);
        zaloUrl = trimToNull(zaloUrl);
        youtubeUrl = trimToNull(youtubeUrl);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
