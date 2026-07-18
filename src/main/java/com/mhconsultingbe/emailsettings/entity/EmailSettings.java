package com.mhconsultingbe.emailsettings.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_settings")
@Getter
@Setter
@NoArgsConstructor
public class EmailSettings {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "singleton_key", nullable = false, unique = true)
    private boolean singletonKey = true;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "from_email", nullable = false, length = 320)
    private String fromEmail;

    @Column(name = "from_name", nullable = false, length = 200)
    private String fromName;

    @Column(name = "consultation_recipient_email", nullable = false, length = 320)
    private String consultationRecipientEmail;

    @Column(name = "smtp_username", length = 320)
    private String smtpUsername;

    @JsonIgnore
    @Column(name = "smtp_password_encrypted", length = 1000)
    private String smtpPasswordEncrypted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void createTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        singletonKey = true;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
        singletonKey = true;
    }
}
