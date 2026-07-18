package com.mhconsultingbe.emailsettings.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

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
        String consultationRecipientEmail,
        @NotBlank
        @Email
        @Size(max = 320)
        String smtpUsername,
        @Size(max = 500)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Schema(
                accessMode = Schema.AccessMode.WRITE_ONLY,
                description = "Leave blank to keep the currently configured SMTP password."
        )
        String smtpPassword
) {
    public EmailSettingsUpdateRequest {
        fromEmail = trimToNull(fromEmail);
        fromName = trimToNull(fromName);
        consultationRecipientEmail = trimToNull(consultationRecipientEmail);
        smtpUsername = trimToNull(smtpUsername);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
