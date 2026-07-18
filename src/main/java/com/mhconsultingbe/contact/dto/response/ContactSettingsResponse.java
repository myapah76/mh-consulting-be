package com.mhconsultingbe.contact.dto.response;

public record ContactSettingsResponse(
        String address,
        String primaryPhone,
        String primaryPhoneLabel,
        String secondaryPhone,
        String secondaryPhoneLabel,
        String email,
        String workingHours,
        String facebookUrl,
        String zaloUrl,
        String youtubeUrl
) {
}
