package com.mhconsultingbe.auth.controller;

import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.auth.dto.response.PasswordResetTokenValidationResponse;
import com.mhconsultingbe.auth.service.PasswordResetOperations;
import com.mhconsultingbe.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PasswordResetController.class,
        properties = "app.frontend-url=http://localhost:3000"
)
@Import(SecurityConfig.class)
class PasswordResetControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordResetOperations service;

    @Test
    void anonymousUserCanRequestPasswordResetWithCsrf() throws Exception {
        when(service.requestReset(any())).thenReturn(new MessageResponse(
                "If the email exists, password reset instructions have been sent"
        ));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"admin@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "If the email exists, password reset instructions have been sent"
                ));
    }

    @Test
    void csrfIsStillRequired() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("{\"email\":\"admin@example.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenValidationUsesExactResponseShape() throws Exception {
        Instant expiresAt = Instant.parse("2026-07-18T02:30:00Z");
        when(service.validateToken(any())).thenReturn(
                new PasswordResetTokenValidationResponse(true, expiresAt)
        );

        mockMvc.perform(post("/api/auth/reset-password/validate")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"token\":\"raw-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.expiresAt").value("2026-07-18T02:30:00Z"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void weakResetPasswordIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "token": "raw-token",
                                  "newPassword": "weakpass",
                                  "confirmPassword": "weakpass"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newPassword").exists());
    }

    @Test
    void passwordMismatchIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "token": "raw-token",
                                  "newPassword": "NewPassword@456",
                                  "confirmPassword": "OtherPassword@789"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.passwordConfirmed").exists());
    }
}
