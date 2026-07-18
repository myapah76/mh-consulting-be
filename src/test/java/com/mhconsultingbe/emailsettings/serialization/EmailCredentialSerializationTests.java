package com.mhconsultingbe.emailsettings.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.entity.EmailSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class EmailCredentialSerializationTests {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void entityAndWriteOnlyRequestDoNotSerializeCredentials() throws Exception {
        EmailSettings settings = new EmailSettings();
        settings.setSmtpUsername("smtp@example.com");
        settings.setSmtpPasswordEncrypted("v1:iv:ciphertext");
        String entityJson = objectMapper.writeValueAsString(settings);
        assertFalse(entityJson.contains("smtpPasswordEncrypted"));
        assertFalse(entityJson.contains("ciphertext"));

        EmailSettingsUpdateRequest request = new EmailSettingsUpdateRequest(
                true,
                "from@example.com",
                "Sender",
                "recipient@example.com",
                "smtp@example.com",
                "raw-password"
        );
        String requestJson = objectMapper.writeValueAsString(request);
        assertFalse(requestJson.contains("smtpPassword"));
        assertFalse(requestJson.contains("raw-password"));
    }
}
