package com.mhconsultingbe.auth.controller;

import com.mhconsultingbe.auth.dto.request.ForgotPasswordRequest;
import com.mhconsultingbe.auth.dto.request.PasswordResetTokenRequest;
import com.mhconsultingbe.auth.dto.request.ResetPasswordRequest;
import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.auth.dto.response.PasswordResetTokenValidationResponse;
import com.mhconsultingbe.auth.service.PasswordResetOperations;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetOperations passwordResetOperations;

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {
        return passwordResetOperations.requestReset(request);
    }

    @PostMapping("/reset-password/validate")
    public PasswordResetTokenValidationResponse validateToken(
            @Valid
            @RequestBody
            PasswordResetTokenRequest request
    ) {
        return passwordResetOperations.validateToken(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {
        return passwordResetOperations.resetPassword(request);
    }
}
