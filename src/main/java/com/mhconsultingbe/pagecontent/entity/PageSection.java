package com.mhconsultingbe.pagecontent.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "page_sections", uniqueConstraints = @UniqueConstraint(name = "uk_page_section", columnNames = {"page_key", "section_key"}))
@Getter @Setter @NoArgsConstructor
public class PageSection {
    @Id @UuidGenerator private UUID id;
    @Column(name = "page_key", nullable = false, length = 100) private String pageKey;
    @Column(name = "section_key", nullable = false, length = 100) private String sectionKey;
    @Column(length = 300) private String title;
    @Column(length = 500) private String subtitle;
    @Column(columnDefinition = "text") private String content;
    @Column(name = "image_url", length = 2000) private String imageUrl;
    @Column(name = "button_label", length = 200) private String buttonLabel;
    @Column(name = "button_url", length = 2000) private String buttonUrl;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void create() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void update() { updatedAt = Instant.now(); }
}
