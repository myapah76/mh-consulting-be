package com.mhconsultingbe.servicecatalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class ServiceListItem {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
