package com.mhconsultingbe.emailsettings.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmailSettingsUpdateRequest(
        @NotNull
        Boolean enabled,
        @NotBlank
        @Email
        @Size(max = 320)
        String fromEmail,
        @NotBlank
        @Size(max = 200)
        String fromName,
        @NotBlank
        @Email
        @Size(max = 320)
        String consultationRecipientEmail
) {
    public EmailSettingsUpdateRequest {
        fromEmail = trimToNull(fromEmail);
        fromName = trimToNull(fromName);
        consultationRecipientEmail = trimToNull(consultationRecipientEmail);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
