package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.request.AdminAccountCreateRequest;
import com.mhconsultingbe.auth.dto.request.ChangePasswordRequest;
import com.mhconsultingbe.auth.entity.Admin;
import com.mhconsultingbe.auth.repository.AdminRepository;
import com.mhconsultingbe.shared.exception.ConflictException;
import com.mhconsultingbe.shared.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAccountServiceTests {
    private static final String CURRENT_PASSWORD = "Current@123";
    private static final String NEW_PASSWORD = "NewPassword@456";

    private final AdminRepository repository = mock(AdminRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AdminAccountService service = new AdminAccountService(repository, passwordEncoder);

    @Test
    void createsActiveAdminWithNormalizedEmailAndBcryptPassword() {
        when(repository.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin admin = invocation.getArgument(0);
            admin.setId(UUID.randomUUID());
            return admin;
        });

        var response = service.create(new AdminAccountCreateRequest(
                "  NewAdmin@Example.COM  ",
                "  New Administrator  ",
                NEW_PASSWORD,
                NEW_PASSWORD
        ));

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(repository).save(captor.capture());
        Admin saved = captor.getValue();
        assertEquals("newadmin@example.com", response.email());
        assertEquals("New Administrator", response.fullName());
        assertEquals("ADMIN", response.role());
        assertTrue(response.active());
        assertNotEquals(NEW_PASSWORD, saved.getPasswordHash());
        assertTrue(saved.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, saved.getPasswordHash()));
    }

    @Test
    void rejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.create(new AdminAccountCreateRequest(
                        " Existing@Example.com ",
                        null,
                        NEW_PASSWORD,
                        NEW_PASSWORD
                ))
        );

        assertEquals("DUPLICATE_ADMIN_EMAIL", exception.getCode());
        verify(repository, never()).save(any());
    }

    @Test
    void changesPasswordAndKeepsBcryptEncoding() {
        Admin admin = admin(CURRENT_PASSWORD);
        String oldHash = admin.getPasswordHash();
        when(repository.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));

        service.changePassword(
                admin.getEmail(),
                new ChangePasswordRequest(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)
        );

        assertNotEquals(oldHash, admin.getPasswordHash());
        assertTrue(admin.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, admin.getPasswordHash()));
        assertFalse(passwordEncoder.matches(CURRENT_PASSWORD, admin.getPasswordHash()));
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        Admin admin = admin(CURRENT_PASSWORD);
        String oldHash = admin.getPasswordHash();
        when(repository.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));

        assertThrows(
                InvalidRequestException.class,
                () -> service.changePassword(
                        admin.getEmail(),
                        new ChangePasswordRequest("Incorrect@123", NEW_PASSWORD, NEW_PASSWORD)
                )
        );

        assertEquals(oldHash, admin.getPasswordHash());
    }

    @Test
    void rejectsSameCurrentAndNewPassword() {
        Admin admin = admin(CURRENT_PASSWORD);
        String oldHash = admin.getPasswordHash();
        when(repository.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));

        assertThrows(
                InvalidRequestException.class,
                () -> service.changePassword(
                        admin.getEmail(),
                        new ChangePasswordRequest(CURRENT_PASSWORD, CURRENT_PASSWORD, CURRENT_PASSWORD)
                )
        );

        assertEquals(oldHash, admin.getPasswordHash());
    }

    private Admin admin(String rawPassword) {
        Admin admin = new Admin();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        return admin;
    }
}
