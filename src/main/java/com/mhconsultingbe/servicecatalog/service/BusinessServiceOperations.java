package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.servicecatalog.dto.ServiceResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceSummaryResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceUpsertRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BusinessServiceOperations {
    Page<ServiceSummaryResponse> list(String category, Boolean active, int page, int size, String sort);

    ServiceResponse publicBySlug(String slug);

    ServiceResponse byId(UUID id);

    ServiceResponse create(ServiceUpsertRequest body);

    ServiceResponse update(UUID id, ServiceUpsertRequest body);

    ServiceResponse setActive(UUID id, boolean active);

    boolean delete(UUID id);
}
