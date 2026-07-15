package com.mhconsultingbe.servicecatalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "business_services")
@Getter @Setter @NoArgsConstructor
public class BusinessService {
    @Id @UuidGenerator private UUID id;
    @Column(nullable = false, unique = true, length = 200) private String slug;
    @Column(nullable = false, length = 200) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ServiceCategory category;
    @Column(name = "short_description", nullable = false, length = 1000) private String shortDescription;
    @Column(length = 100) private String icon;
    @Column(name = "full_content", columnDefinition = "text") private String fullContent;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<ServiceDetailPoint> detailedPoints = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<ServiceBenefit> benefits = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<ServiceProcessStep> processSteps = new ArrayList<>();

    @PrePersist void create() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void update() { updatedAt = Instant.now(); }
}
