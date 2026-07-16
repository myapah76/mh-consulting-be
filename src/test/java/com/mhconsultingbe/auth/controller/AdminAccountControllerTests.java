package com.mhconsultingbe.auth.controller;

import com.mhconsultingbe.auth.dto.request.AdminAccountCreateRequest;
import com.mhconsultingbe.auth.dto.response.AdminAccountResponse;
import com.mhconsultingbe.auth.service.AdminAccountOperations;
import com.mhconsultingbe.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminAccountController.class,
        properties = "app.frontend-url=http://localhost:3000"
)
@Import(SecurityConfig.class)
class AdminAccountControllerTests {
    private static final String CREATE_BODY = """
            {
              "email": "newadmin@example.com",
              "fullName": "New Administrator",
              "password": "StrongPassword@123",
              "confirmPassword": "StrongPassword@123"
            }
            """;
    private static final String CHANGE_BODY = """
            {
              "currentPassword": "CurrentPassword@123",
              "newPassword": "NewPassword@456",
              "confirmPassword": "NewPassword@456"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAccountOperations service;

    @Test
    void successfulCreationReturnsCreatedSafeResponse() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create(any(AdminAccountCreateRequest.class))).thenReturn(
                new AdminAccountResponse(
                        id,
                        "newadmin@example.com",
                        "New Administrator",
                        "ADMIN",
                        true
                )
        );

        mockMvc.perform(post("/api/admin/accounts")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("newadmin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void invalidEmailIsRejected() throws Exception {
        performCreate(CREATE_BODY.replace("newadmin@example.com", "invalid-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void weakPasswordIsRejected() throws Exception {
        performCreate(CREATE_BODY.replace("StrongPassword@123", "weakpass"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void createPasswordConfirmationMismatchIsRejected() throws Exception {
        performCreate(CREATE_BODY.replace(
                "\"confirmPassword\": \"StrongPassword@123\"",
                "\"confirmPassword\": \"DifferentPassword@123\""
        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.passwordConfirmed").exists());
    }

    @Test
    void unauthenticatedCreationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/accounts")
                        .with(csrf())
                        .contentType("application/json")
                        .content(CREATE_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCreationReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/accounts")
                        .with(user("user@example.com").roles("USER"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(CREATE_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void successfulPasswordChangeUsesAuthenticatedIdentity() throws Exception {
        mockMvc.perform(put("/api/admin/accounts/me/password")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(CHANGE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(service).changePassword(
                org.mockito.ArgumentMatchers.eq("admin@example.com"),
                any()
        );
    }

    @Test
    void weakNewPasswordIsRejected() throws Exception {
        performChange(CHANGE_BODY.replace("NewPassword@456", "weakpass"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newPassword").exists());
    }

    @Test
    void newPasswordConfirmationMismatchIsRejected() throws Exception {
        performChange(CHANGE_BODY.replace(
                "\"confirmPassword\": \"NewPassword@456\"",
                "\"confirmPassword\": \"DifferentPassword@456\""
        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.passwordConfirmed").exists());
    }

    @Test
    void unauthenticatedPasswordChangeReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/admin/accounts/me/password")
                        .with(csrf())
                        .contentType("application/json")
                        .content(CHANGE_BODY))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions performCreate(String content) throws Exception {
        return mockMvc.perform(post("/api/admin/accounts")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content(content));
    }

    private org.springframework.test.web.servlet.ResultActions performChange(String content) throws Exception {
        return mockMvc.perform(put("/api/admin/accounts/me/password")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content(content));
    }
}
