package com.mhconsultingbe.emailsettings.security;

import com.mhconsultingbe.emailsettings.exception.EmailCredentialConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EmailCredentialCipher {
    private static final String VERSION = "v1";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final byte[] key;
    private final SecureRandom secureRandom;

    @Autowired
    public EmailCredentialCipher(
            @Value("${app.mail.credentials-encryption-key:}")
            String encodedKey
    ) {
        this(encodedKey, new SecureRandom());
    }

    EmailCredentialCipher(String encodedKey, SecureRandom secureRandom) {
        this.key = decodeKey(encodedKey);
        this.secureRandom = secureRandom;
    }

    public boolean isAvailable() {
        return key != null;
    }

    public String encrypt(String plaintext) {
        requireAvailable();
        if (plaintext == null) {
            throw new EmailCredentialConfigurationException("SMTP credential cannot be encrypted");
        }

        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, iv)
            );
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            Base64.Encoder encoder = Base64.getEncoder();
            return VERSION + ":" + encoder.encodeToString(iv) + ":" + encoder.encodeToString(ciphertext);
        } catch (GeneralSecurityException exception) {
            throw new EmailCredentialConfigurationException("SMTP credential encryption failed", exception);
        }
    }

    public String decrypt(String encryptedPayload) {
        requireAvailable();
        String[] parts = encryptedPayload == null ? new String[0] : encryptedPayload.split(":", -1);
        if (parts.length != 3 || !VERSION.equals(parts[0])) {
            throw malformed(null);
        }

        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);
            if (iv.length != IV_BYTES || ciphertext.length < 16) {
                throw malformed(null);
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, iv)
            );
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (AEADBadTagException | IllegalArgumentException exception) {
            throw malformed(exception);
        } catch (GeneralSecurityException exception) {
            throw malformed(exception);
        }
    }

    private void requireAvailable() {
        if (!isAvailable()) {
            throw new EmailCredentialConfigurationException(
                    "SMTP credential management is unavailable because the encryption key is not configured"
            );
        }
    }

    private EmailCredentialConfigurationException malformed(Throwable cause) {
        return new EmailCredentialConfigurationException("Stored SMTP credential cannot be decrypted", cause);
    }

    private static byte[] decodeKey(String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedKey.trim());
            if (decoded.length != KEY_BYTES) {
                throw new EmailCredentialConfigurationException(
                        "SMTP credential encryption key must decode to exactly 32 bytes"
                );
            }
            return decoded;
        } catch (IllegalArgumentException exception) {
            throw new EmailCredentialConfigurationException(
                    "SMTP credential encryption key must be valid Base64",
                    exception
            );
        }
    }
}
