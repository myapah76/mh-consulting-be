package com.mhconsultingbe.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ContactSettingsRequest(
        @Size(max=300) String companyName, @Size(max=500) String tagline,
        @Size(max=50) String primaryPhone, @Size(max=100) String hotline1,
        @Size(max=100) String hotline2, @Size(max=100) String hotline3,
        @Email @Size(max=320) String email, @Size(max=1000) String address,
        @Size(max=2000) String zaloUrl, @Size(max=2000) String facebookUrl,
        @Size(max=2000) String googleMapsUrl, @Size(max=2000) String businessRegistrationText
) {}
