package com.mhconsultingbe.servicecatalog.service;

import java.util.Optional;
import java.util.UUID;

public interface ServiceCatalogQuery {
    Optional<ServiceReference> findActiveReference(UUID id);

    record ServiceReference(UUID id, String title) {
    }
}
