package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.request.AdminAccountCreateRequest;
import com.mhconsultingbe.auth.dto.request.ChangePasswordRequest;
import com.mhconsultingbe.auth.dto.response.AdminAccountResponse;
import com.mhconsultingbe.auth.entity.Admin;
import com.mhconsultingbe.auth.entity.AdminRole;
import com.mhconsultingbe.auth.repository.AdminRepository;
import com.mhconsultingbe.shared.exception.ConflictException;
import com.mhconsultingbe.shared.exception.InvalidRequestException;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountService implements AdminAccountOperations {
    private final AdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminAccountResponse create(AdminAccountCreateRequest request) {
        String email = TextNormalizer.lowercase(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("DUPLICATE_ADMIN_EMAIL", "An admin account with this email already exists");
        }

        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setFullName(TextNormalizer.plainText(request.fullName()));
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setRole(AdminRole.ADMIN);
        admin.setActive(true);
        return response(repository.save(admin));
    }

    @Override
    @Transactional
    public void changePassword(String authenticatedEmail, ChangePasswordRequest request) {
        Admin admin = repository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated admin no longer exists"));
        if (!passwordEncoder.matches(request.currentPassword(), admin.getPasswordHash())) {
            throw new InvalidRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), admin.getPasswordHash())) {
            throw new InvalidRequestException("New password must be different from the current password");
        }
        admin.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private AdminAccountResponse response(Admin admin) {
        return new AdminAccountResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                admin.getRole().name(),
                admin.isActive()
        );
    }
}
