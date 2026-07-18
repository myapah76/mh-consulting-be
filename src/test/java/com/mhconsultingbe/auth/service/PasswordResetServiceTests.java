package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.request.ForgotPasswordRequest;
import com.mhconsultingbe.auth.dto.request.PasswordResetTokenRequest;
import com.mhconsultingbe.auth.dto.request.ResetPasswordRequest;
import com.mhconsultingbe.auth.entity.Admin;
import com.mhconsultingbe.auth.entity.PasswordResetToken;
import com.mhconsultingbe.auth.mail.AdminPasswordResetMailService;
import com.mhconsultingbe.auth.repository.AdminRepository;
import com.mhconsultingbe.auth.repository.PasswordResetTokenRepository;
import com.mhconsultingbe.shared.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTests {
    private static final Instant NOW = Instant.parse("2026-07-18T02:00:00Z");
    private static final String CURRENT_PASSWORD = "CurrentPassword@123";
    private static final String NEW_PASSWORD = "NewPassword@456";
    private static final String GENERIC =
            "If the email exists, password reset instructions have been sent";

    private final AdminRepository adminRepository = mock(AdminRepository.class);
    private final PasswordResetTokenRepository tokenRepository =
            mock(PasswordResetTokenRepository.class);
    private final AdminPasswordResetMailService mailService =
            mock(AdminPasswordResetMailService.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordResetService service = new PasswordResetService(
            adminRepository,
            tokenRepository,
            mailService,
            passwordEncoder,
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @BeforeEach
    void configureResetUrl() {
        ReflectionTestUtils.setField(
                service,
                "passwordResetUrl",
                "https://mh-consulting-five.vercel.app/admin/reset-password"
        );
    }

    @Test
    void unknownEmailReturnsGenericResponseWithoutTokenOrEmail() {
        when(adminRepository.findByEmailIgnoreCaseForUpdate("missing@example.com"))
                .thenReturn(Optional.empty());

        var response = service.requestReset(new ForgotPasswordRequest(" Missing@Example.com "));

        assertEquals(GENERIC, response.message());
        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).send(anyString(), anyString(), any(Integer.class));
    }

    @Test
    void activeAdminGetsHashedTokenAndFrontendLink() throws Exception {
        Admin admin = admin(true);
        when(adminRepository.findByEmailIgnoreCaseForUpdate(admin.getEmail()))
                .thenReturn(Optional.of(admin));

        var response = service.requestReset(new ForgotPasswordRequest(" Admin@Example.com "));

        assertEquals(GENERIC, response.message());
        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(mailService).send(org.mockito.ArgumentMatchers.eq(admin.getEmail()), urlCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(30));

        PasswordResetToken stored = tokenCaptor.getValue();
        String rawToken = urlCaptor.getValue().substring(urlCaptor.getValue().indexOf("?token=") + 7);
        assertEquals(64, stored.getTokenHash().length());
        assertEquals(hash(rawToken), stored.getTokenHash());
        assertFalse(stored.getTokenHash().equals(rawToken));
        assertEquals(NOW.plusSeconds(1800), stored.getExpiresAt());
        assertEquals(NOW, stored.getCreatedAt());
    }

    @Test
    void cooldownReturnsSameGenericResponse() {
        Admin admin = admin(true);
        when(adminRepository.findByEmailIgnoreCaseForUpdate(admin.getEmail()))
                .thenReturn(Optional.of(admin));
        when(tokenRepository.existsByAdminIdAndCreatedAtAfter(any(), any())).thenReturn(true);

        var response = service.requestReset(new ForgotPasswordRequest(admin.getEmail()));

        assertEquals(GENERIC, response.message());
        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).send(anyString(), anyString(), any(Integer.class));
    }

    @Test
    void expiredTokenValidatesAsInvalidWithoutAccountDetails() {
        PasswordResetToken token = token(admin(true), NOW.minusSeconds(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        var response = service.validateToken(new PasswordResetTokenRequest("raw-token"));

        assertFalse(response.valid());
        assertNull(response.expiresAt());
    }

    @Test
    void validTokenResetsPasswordAndConsumesAllAdminTokens() {
        Admin admin = admin(true);
        PasswordResetToken token = token(admin, NOW.plusSeconds(60));
        when(tokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));

        var response = service.resetPassword(
                new ResetPasswordRequest("raw-token", NEW_PASSWORD, NEW_PASSWORD)
        );

        assertEquals("Password reset successfully", response.message());
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, admin.getPasswordHash()));
        verify(tokenRepository).markUnusedTokensUsed(admin.getId(), NOW);
    }

    @Test
    void usedTokenHasSameSafeErrorAsMissingToken() {
        PasswordResetToken token = token(admin(true), NOW.plusSeconds(60));
        token.setUsedAt(NOW.minusSeconds(1));
        when(tokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> service.resetPassword(
                        new ResetPasswordRequest("raw-token", NEW_PASSWORD, NEW_PASSWORD)
                )
        );

        assertEquals("Password reset link is invalid or has expired", exception.getMessage());
    }

    @Test
    void currentPasswordCannotBeReused() {
        Admin admin = admin(true);
        PasswordResetToken token = token(admin, NOW.plusSeconds(60));
        when(tokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));

        assertThrows(
                InvalidRequestException.class,
                () -> service.resetPassword(new ResetPasswordRequest(
                        "raw-token",
                        CURRENT_PASSWORD,
                        CURRENT_PASSWORD
                ))
        );
        verify(tokenRepository, never()).markUnusedTokensUsed(any(), any());
    }

    private Admin admin(boolean active) {
        Admin admin = new Admin();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode(CURRENT_PASSWORD));
        admin.setActive(active);
        return admin;
    }

    private PasswordResetToken token(Admin admin, Instant expiresAt) {
        PasswordResetToken token = new PasswordResetToken();
        token.setAdmin(admin);
        token.setTokenHash("hash");
        token.setCreatedAt(NOW.minusSeconds(30));
        token.setExpiresAt(expiresAt);
        return token;
    }

    private String hash(String token) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(token.getBytes(StandardCharsets.UTF_8))
        );
    }
}
