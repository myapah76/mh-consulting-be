package com.mhconsultingbe.emailsettings.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestEmailRequest(
        @NotBlank
        @Email
        @Size(max = 320)
        String recipientEmail
) {
    public TestEmailRequest {
        if (recipientEmail != null) {
            recipientEmail = recipientEmail.trim();
        }
    }
}
