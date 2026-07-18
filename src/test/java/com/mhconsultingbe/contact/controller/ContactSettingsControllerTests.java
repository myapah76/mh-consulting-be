package com.mhconsultingbe.contact.controller;

import com.mhconsultingbe.contact.dto.request.ContactSettingsRequest;
import com.mhconsultingbe.contact.dto.response.ContactSettingsResponse;
import com.mhconsultingbe.contact.service.ContactSettingsOperations;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ContactSettingsController.class,
        properties = "app.frontend-url=http://localhost:3000"
)
@Import(SecurityConfig.class)
class ContactSettingsControllerTests {
    private static final String VALID_REQUEST = """
            {
              "address": "133/15 Đ. Ngô Đức Kế, TP. Hồ Chí Minh",
              "primaryPhone": "0903.024.116",
              "primaryPhoneLabel": "Ms. Thảo",
              "secondaryPhone": "0938.835.633",
              "secondaryPhoneLabel": "Mr. Trí",
              "email": "info@mhconsulting.vn",
              "workingHours": "Thứ 2 - Thứ 7: 08:00 - 17:30",
              "facebookUrl": null,
              "zaloUrl": "https://zalo.me/0903024116",
              "youtubeUrl": null
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactSettingsOperations contactSettingsOperations;

    @Test
    void publicRetrievalReturnsExactFieldsWithoutAuthentication() throws Exception {
        when(contactSettingsOperations.get()).thenReturn(response());

        mockMvc.perform(get("/api/public/contact-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.primaryPhone").value("0903.024.116"))
                .andExpect(jsonPath("$.primaryPhoneLabel").value("Ms. Thảo"))
                .andExpect(jsonPath("$.secondaryPhone").value("0938.835.633"))
                .andExpect(jsonPath("$.secondaryPhoneLabel").value("Mr. Trí"))
                .andExpect(jsonPath("$.email").value("info@mhconsulting.vn"))
                .andExpect(jsonPath("$.workingHours").exists())
                .andExpect(jsonPath("$.facebookUrl").isEmpty())
                .andExpect(jsonPath("$.zaloUrl").value("https://zalo.me/0903024116"))
                .andExpect(jsonPath("$.youtubeUrl").isEmpty())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void authenticatedAdminCanRetrieveSettings() throws Exception {
        when(contactSettingsOperations.get()).thenReturn(response());

        mockMvc.perform(get("/api/admin/contact-settings")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("info@mhconsulting.vn"));
    }

    @Test
    void validAdminUpdateWithCsrfSucceeds() throws Exception {
        when(contactSettingsOperations.update(any(ContactSettingsRequest.class)))
                .thenReturn(response());

        performAdminUpdate(VALID_REQUEST)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryPhone").value("0903.024.116"));
    }

    @Test
    void requiredAndFormattedFieldsAreValidated() throws Exception {
        String invalid = VALID_REQUEST
                .replace("133/15 Đ. Ngô Đức Kế, TP. Hồ Chí Minh", " ")
                .replace("0903.024.116", "invalid-phone")
                .replace("info@mhconsulting.vn", "invalid-email")
                .replace("Thứ 2 - Thứ 7: 08:00 - 17:30", " ")
                .replace("https://zalo.me/0903024116", "/relative");

        performAdminUpdate(invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.address").exists())
                .andExpect(jsonPath("$.fieldErrors.primaryPhone").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.workingHours").exists())
                .andExpect(jsonPath("$.fieldErrors.zaloUrl").exists());
    }

    @Test
    void optionalPhoneAndAllSocialUrlsAreValidated() throws Exception {
        String invalid = VALID_REQUEST
                .replace("0938.835.633", "invalid-phone")
                .replace("\"facebookUrl\": null", "\"facebookUrl\": \"javascript:alert(1)\"")
                .replace("https://zalo.me/0903024116", "data:text/plain,bad")
                .replace("\"youtubeUrl\": null", "\"youtubeUrl\": \"/relative\"");

        performAdminUpdate(invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.secondaryPhone").exists())
                .andExpect(jsonPath("$.fieldErrors.facebookUrl").exists())
                .andExpect(jsonPath("$.fieldErrors.zaloUrl").exists())
                .andExpect(jsonPath("$.fieldErrors.youtubeUrl").exists());
    }

    @Test
    void unauthenticatedAndNonAdminRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/admin/contact-settings"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/contact-settings")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPutWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(put("/api/admin/contact-settings")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .contentType("application/json")
                        .content(VALID_REQUEST))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions performAdminUpdate(String body) throws Exception {
        return mockMvc.perform(put("/api/admin/contact-settings")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content(body));
    }

    private ContactSettingsResponse response() {
        return new ContactSettingsResponse(
                "133/15 Đ. Ngô Đức Kế, TP. Hồ Chí Minh",
                "0903.024.116",
                "Ms. Thảo",
                "0938.835.633",
                "Mr. Trí",
                "info@mhconsulting.vn",
                "Thứ 2 - Thứ 7: 08:00 - 17:30",
                null,
                "https://zalo.me/0903024116",
                null
        );
    }
}
