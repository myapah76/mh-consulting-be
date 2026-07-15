package com.mhconsultingbe.consultation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consultation_requests")
@Getter
@Setter
@NoArgsConstructor
public class ConsultationRequest {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(length = 320)
    private String email;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "service_title_snapshot", length = 200)
    private String serviceTitleSnapshot;

    @Column(columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsultationStatus status = ConsultationStatus.NEW;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void create() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void update() {
        updatedAt = Instant.now();
    }
}
