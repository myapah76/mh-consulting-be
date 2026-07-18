package com.mhconsultingbe.emailsettings.security;

import com.mhconsultingbe.emailsettings.exception.EmailCredentialConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailCredentialCipherTests {
    private static final String PASSWORD = "abcdefghijklmnop";

    @Test
    void encryptsAndDecryptsWithFreshRandomIv() {
        EmailCredentialCipher cipher = cipher((byte) 1);

        String first = cipher.encrypt(PASSWORD);
        String second = cipher.encrypt(PASSWORD);

        assertEquals(PASSWORD, cipher.decrypt(first));
        assertEquals(PASSWORD, cipher.decrypt(second));
        assertNotEquals(first, second);
        assertTrue(first.startsWith("v1:"));
        assertFalse(first.contains(PASSWORD));
    }

    @Test
    void wrongKeyAndTamperingCannotDecrypt() {
        String encrypted = cipher((byte) 1).encrypt(PASSWORD);
        assertThrows(
                EmailCredentialConfigurationException.class,
                () -> cipher((byte) 2).decrypt(encrypted)
        );

        String tampered = encrypted.substring(0, encrypted.length() - 2) + "AA";
        assertThrows(
                EmailCredentialConfigurationException.class,
                () -> cipher((byte) 1).decrypt(tampered)
        );
    }

    @Test
    void missingKeyAndMalformedPayloadFailSafely() {
        EmailCredentialCipher unavailable = new EmailCredentialCipher("");
        assertFalse(unavailable.isAvailable());
        assertThrows(
                EmailCredentialConfigurationException.class,
                () -> unavailable.encrypt(PASSWORD)
        );
        assertThrows(
                EmailCredentialConfigurationException.class,
                () -> cipher((byte) 1).decrypt("not-a-versioned-payload")
        );
    }

    private EmailCredentialCipher cipher(byte value) {
        byte[] key = new byte[32];
        java.util.Arrays.fill(key, value);
        return new EmailCredentialCipher(Base64.getEncoder().encodeToString(key));
    }
}
