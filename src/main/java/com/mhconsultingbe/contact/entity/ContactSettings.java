package com.mhconsultingbe.contact.entity;

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
@Table(name = "contact_settings")
@Getter
@Setter
@NoArgsConstructor
public class ContactSettings {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "singleton_key", nullable = false, unique = true)
    private boolean singletonKey = true;

    @Column(length = 1000, nullable = false)
    private String address;

    @Column(name = "primary_phone", length = 50, nullable = false)
    private String primaryPhone;

    @Column(name = "primary_phone_label", length = 100)
    private String primaryPhoneLabel;

    @Column(name = "secondary_phone", length = 50)
    private String secondaryPhone;

    @Column(name = "secondary_phone_label", length = 100)
    private String secondaryPhoneLabel;

    @Column(length = 320, nullable = false)
    private String email;

    @Column(name = "working_hours", length = 255, nullable = false)
    private String workingHours;

    @Column(name = "facebook_url", length = 2000)
    private String facebookUrl;

    @Column(name = "zalo_url", length = 2000)
    private String zaloUrl;

    @Column(name = "youtube_url", length = 1000)
    private String youtubeUrl;

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
