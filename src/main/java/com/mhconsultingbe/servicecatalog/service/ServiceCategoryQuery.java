package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.servicecatalog.dto.ServiceCategoryResponse;

import java.util.List;

public interface ServiceCategoryQuery {
    List<ServiceCategoryResponse> listActive();
}
