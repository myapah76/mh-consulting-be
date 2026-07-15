package com.mhconsultingbe.contact.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "contact_settings")
@Getter @Setter @NoArgsConstructor
public class ContactSettings {
    @Id @UuidGenerator private UUID id;
    @Column(name = "singleton_key", nullable = false, unique = true) private boolean singletonKey = true;
    @Column(name = "company_name", length = 300) private String companyName;
    @Column(length = 500) private String tagline;
    @Column(name = "primary_phone", length = 50) private String primaryPhone;
    @Column(length = 100) private String hotline1;
    @Column(length = 100) private String hotline2;
    @Column(length = 100) private String hotline3;
    @Column(length = 320) private String email;
    @Column(length = 1000) private String address;
    @Column(name = "zalo_url", length = 2000) private String zaloUrl;
    @Column(name = "facebook_url", length = 2000) private String facebookUrl;
    @Column(name = "google_maps_url", length = 2000) private String googleMapsUrl;
    @Column(name = "business_registration_text", length = 2000) private String businessRegistrationText;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist @PreUpdate void updateTime() { updatedAt = Instant.now(); singletonKey = true; }
}
