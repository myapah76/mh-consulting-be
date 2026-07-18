package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.request.ForgotPasswordRequest;
import com.mhconsultingbe.auth.dto.request.PasswordResetTokenRequest;
import com.mhconsultingbe.auth.dto.request.ResetPasswordRequest;
import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.auth.dto.response.PasswordResetTokenValidationResponse;
import com.mhconsultingbe.auth.entity.Admin;
import com.mhconsultingbe.auth.entity.PasswordResetToken;
import com.mhconsultingbe.auth.mail.AdminPasswordResetMailService;
import com.mhconsultingbe.auth.repository.AdminRepository;
import com.mhconsultingbe.auth.repository.PasswordResetTokenRepository;
import com.mhconsultingbe.shared.exception.InvalidRequestException;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetService implements PasswordResetOperations {
    private static final String GENERIC_RESPONSE =
            "If the email exists, password reset instructions have been sent";
    private static final String INVALID_TOKEN = "Password reset link is invalid or has expired";
    private static final String RESET_SUCCESS = "Password reset successfully";
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
    private static final Duration REQUEST_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration USED_TOKEN_RETENTION = Duration.ofDays(7);
    private static final int TOKEN_BYTES = 32;

    private final AdminRepository adminRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final AdminPasswordResetMailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset-url}")
    private String passwordResetUrl;

    @Override
    @Transactional
    public MessageResponse requestReset(ForgotPasswordRequest request) {
        Instant now = clock.instant();
        tokenRepository.deleteStaleTokens(now, now.minus(USED_TOKEN_RETENTION));

        String email = TextNormalizer.lowercase(request.email());
        Admin admin = adminRepository.findByEmailIgnoreCaseForUpdate(email).orElse(null);
        if (admin == null || !admin.isActive()) {
            return genericResponse();
        }
        if (tokenRepository.existsByAdminIdAndCreatedAtAfter(
                admin.getId(),
                now.minus(REQUEST_COOLDOWN)
        )) {
            return genericResponse();
        }

        tokenRepository.markUnusedTokensUsed(admin.getId(), now);
        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setAdmin(admin);
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(TOKEN_LIFETIME));
        tokenRepository.save(token);

        mailService.send(admin.getEmail(), resetUrl(rawToken), (int) TOKEN_LIFETIME.toMinutes());
        return genericResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordResetTokenValidationResponse validateToken(PasswordResetTokenRequest request) {
        Instant now = clock.instant();
        return tokenRepository.findByTokenHash(hash(request.token()))
                .filter(token -> isUsable(token, now))
                .map(token -> new PasswordResetTokenValidationResponse(true, token.getExpiresAt()))
                .orElseGet(() -> new PasswordResetTokenValidationResponse(false, null));
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        Instant now = clock.instant();
        PasswordResetToken token = tokenRepository.findByTokenHashForUpdate(hash(request.token()))
                .filter(candidate -> isUsable(candidate, now))
                .orElseThrow(() -> new InvalidRequestException(INVALID_TOKEN));
        Admin admin = token.getAdmin();
        if (passwordEncoder.matches(request.newPassword(), admin.getPasswordHash())) {
            throw new InvalidRequestException(
                    "New password must be different from the current password"
            );
        }

        admin.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        tokenRepository.markUnusedTokensUsed(admin.getId(), now);
        return new MessageResponse(RESET_SUCCESS);
    }

    private boolean isUsable(PasswordResetToken token, Instant now) {
        return token.getUsedAt() == null
                && token.getExpiresAt().isAfter(now)
                && token.getAdmin().isActive();
    }

    private MessageResponse genericResponse() {
        return new MessageResponse(GENERIC_RESPONSE);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String resetUrl(String rawToken) {
        String baseUrl = passwordResetUrl.endsWith("/")
                ? passwordResetUrl.substring(0, passwordResetUrl.length() - 1)
                : passwordResetUrl;
        return baseUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }
}
