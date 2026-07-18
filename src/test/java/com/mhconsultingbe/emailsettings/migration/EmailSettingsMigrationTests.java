package com.mhconsultingbe.emailsettings.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailSettingsMigrationTests {
    @Test
    void migrationCreatesOneSingletonDefaultWithoutSecrets() throws IOException {
        String sql;
        try (var stream = getClass().getResourceAsStream(
                "/db/migration/V7__create_email_settings.sql"
        )) {
            if (stream == null) {
                throw new IOException("Email settings migration not found");
            }
            sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        String normalized = sql.toLowerCase(java.util.Locale.ROOT);
        assertTrue(normalized.contains("unique check (singleton_key)"));
        assertTrue(normalized.contains("where not exists (select 1 from email_settings)"));
        assertTrue(normalized.contains("insert into email_settings"));
        assertFalse(normalized.contains("smtp_password"));
        assertFalse(normalized.contains("smtp_username"));
        assertFalse(normalized.contains("api_key"));
        assertFalse(normalized.contains("encryption_key"));
    }
}
