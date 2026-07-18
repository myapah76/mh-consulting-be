package com.mhconsultingbe.emailsettings.controller;

import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.dto.request.TestEmailRequest;
import com.mhconsultingbe.emailsettings.dto.response.EmailSettingsResponse;
import com.mhconsultingbe.emailsettings.exception.EmailDeliveryFailedException;
import com.mhconsultingbe.emailsettings.service.EmailSettingsOperations;
import com.mhconsultingbe.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminEmailSettingsController.class,
        properties = "app.frontend-url=http://localhost:3000"
)
@Import(SecurityConfig.class)
class AdminEmailSettingsControllerTests {
    private static final String UPDATE_REQUEST = """
            {
              "enabled": true,
              "fromEmail": "info@mhconsulting.vn",
              "fromName": "MH Consulting",
              "consultationRecipientEmail": "myapah7605@gmail.com",
              "smtpUsername": "myapah7605@gmail.com",
              "smtpPassword": "new-app-password"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailSettingsOperations emailSettingsOperations;

    @Test
    void adminRetrievalReturnsExactSafeContract() throws Exception {
        when(emailSettingsOperations.get()).thenReturn(response());

        mockMvc.perform(get("/api/admin/email-settings")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.fromEmail").value("info@mhconsulting.vn"))
                .andExpect(jsonPath("$.fromName").value("MH Consulting"))
                .andExpect(jsonPath("$.consultationRecipientEmail").value("myapah7605@gmail.com"))
                .andExpect(jsonPath("$.deliveryProvider").value("SMTP"))
                .andExpect(jsonPath("$.providerConfigured").value(true))
                .andExpect(jsonPath("$.smtpUsername").value("myapah7605@gmail.com"))
                .andExpect(jsonPath("$.smtpPasswordConfigured").value(true))
                .andExpect(jsonPath("$.smtpPassword").doesNotExist())
                .andExpect(jsonPath("$.smtpPasswordEncrypted").doesNotExist())
                .andExpect(jsonPath("$.host").doesNotExist())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void adminCanUpdateWithCsrf() throws Exception {
        when(emailSettingsOperations.update(any(EmailSettingsUpdateRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/admin/email-settings")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(UPDATE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryProvider").value("SMTP"))
                .andExpect(jsonPath("$.smtpPassword").doesNotExist())
                .andExpect(jsonPath("$.smtpPasswordEncrypted").doesNotExist());
    }

    @Test
    void adminCanSendTestWithCsrf() throws Exception {
        when(emailSettingsOperations.sendTestEmail(any(TestEmailRequest.class)))
                .thenReturn(new MessageResponse("Test email sent successfully"));

        mockMvc.perform(post("/api/admin/email-settings/test")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"recipientEmail\":\"admin@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test email sent successfully"));
    }

    @Test
    void invalidSettingsAndRecipientReturnFieldErrors() throws Exception {
        mockMvc.perform(put("/api/admin/email-settings")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "enabled": null,
                                  "fromEmail": "invalid",
                                  "fromName": " ",
                                  "consultationRecipientEmail": "invalid",
                                  "smtpUsername": "invalid"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.enabled").exists())
                .andExpect(jsonPath("$.fieldErrors.fromEmail").exists())
                .andExpect(jsonPath("$.fieldErrors.fromName").exists())
                .andExpect(jsonPath("$.fieldErrors.consultationRecipientEmail").exists())
                .andExpect(jsonPath("$.fieldErrors.smtpUsername").exists());

        mockMvc.perform(post("/api/admin/email-settings/test")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"recipientEmail\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.recipientEmail").exists());
    }

    @Test
    void providerFailureUsesSafeGatewayResponse() throws Exception {
        when(emailSettingsOperations.sendTestEmail(any(TestEmailRequest.class)))
                .thenThrow(new EmailDeliveryFailedException("Unable to send test email"));

        mockMvc.perform(post("/api/admin/email-settings/test")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"recipientEmail\":\"admin@example.com\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("EMAIL_DELIVERY_FAILED"))
                .andExpect(jsonPath("$.message").value("Unable to send test email"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void unauthenticatedAndNonAdminAccessIsRejected() throws Exception {
        mockMvc.perform(get("/api/admin/email-settings"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/email-settings")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void putAndPostRequireCsrf() throws Exception {
        mockMvc.perform(put("/api/admin/email-settings")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .contentType("application/json")
                        .content(UPDATE_REQUEST))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/email-settings/test")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .contentType("application/json")
                        .content("{\"recipientEmail\":\"admin@example.com\"}"))
                .andExpect(status().isForbidden());
    }

    private EmailSettingsResponse response() {
        return new EmailSettingsResponse(
                true,
                "info@mhconsulting.vn",
                "MH Consulting",
                "myapah7605@gmail.com",
                "SMTP",
                true,
                "myapah7605@gmail.com",
                true
        );
    }
}
