package com.mhconsultingbe.contact.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ContactSettingsResponse(
        UUID id,
        String companyName,
        String tagline,
        String primaryPhone,
        String hotline1,
        String hotline2,
        String hotline3,
        String email,
        String address,
        String zaloUrl,
        String facebookUrl,
        String googleMapsUrl,
        String businessRegistrationText,
        Instant updatedAt
) {
}
