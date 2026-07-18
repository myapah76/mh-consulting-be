package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.request.ForgotPasswordRequest;
import com.mhconsultingbe.auth.dto.request.PasswordResetTokenRequest;
import com.mhconsultingbe.auth.dto.request.ResetPasswordRequest;
import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.auth.dto.response.PasswordResetTokenValidationResponse;

public interface PasswordResetOperations {
    MessageResponse requestReset(ForgotPasswordRequest request);

    PasswordResetTokenValidationResponse validateToken(PasswordResetTokenRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);
}
