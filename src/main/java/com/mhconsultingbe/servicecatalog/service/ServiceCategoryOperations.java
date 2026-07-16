package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.servicecatalog.dto.request.ServiceCategoryUpsertRequest;
import com.mhconsultingbe.servicecatalog.dto.response.AdminServiceCategoryResponse;
import com.mhconsultingbe.servicecatalog.dto.response.ServiceCategoryDeleteResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ServiceCategoryOperations {
    Page<AdminServiceCategoryResponse> list(Boolean active, int page, int size, String sort);

    AdminServiceCategoryResponse getById(UUID id);

    AdminServiceCategoryResponse create(ServiceCategoryUpsertRequest request);

    AdminServiceCategoryResponse update(UUID id, ServiceCategoryUpsertRequest request);

    AdminServiceCategoryResponse updateActive(UUID id, boolean active);

    ServiceCategoryDeleteResponse delete(UUID id);
}
