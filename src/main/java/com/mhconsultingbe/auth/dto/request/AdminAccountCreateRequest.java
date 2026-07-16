package com.mhconsultingbe.auth.dto.request;

import com.mhconsultingbe.shared.validation.ValidationPatterns;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminAccountCreateRequest(
        @NotBlank
        @Email
        @Size(max = 320)
        String email,
        @Size(max = 200)
        String fullName,
        @NotBlank
        @Size(min = 8, max = 200)
        @Pattern(
                regexp = ValidationPatterns.STRONG_PASSWORD,
                message = "must contain uppercase, lowercase, digit, and special characters"
        )
        String password,
        @NotBlank
        @Size(max = 200)
        String confirmPassword
) {
    @AssertTrue(message = "password and confirmPassword must match")
    public boolean isPasswordConfirmed() {
        return password == null || confirmPassword == null || password.equals(confirmPassword);
    }
}
