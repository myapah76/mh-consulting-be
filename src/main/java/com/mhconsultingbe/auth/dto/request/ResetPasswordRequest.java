package com.mhconsultingbe.auth.dto.request;

import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank
        @Size(max = 500)
        String token,
        @NotBlank
        @Size(min = 8, max = 200)
        @Pattern(
                regexp = ValidationPatterns.STRONG_PASSWORD,
                message = "must contain uppercase, lowercase, digit, and special characters"
        )
        String newPassword,
        @NotBlank
        @Size(max = 200)
        String confirmPassword
) {
    @AssertTrue(message = "newPassword and confirmPassword must match")
    public boolean isPasswordConfirmed() {
        return newPassword == null || confirmPassword == null || newPassword.equals(confirmPassword);
    }
}
