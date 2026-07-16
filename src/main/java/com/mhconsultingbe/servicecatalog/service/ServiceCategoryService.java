package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.servicecatalog.dto.ServiceCategoryResponse;
import com.mhconsultingbe.servicecatalog.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService implements ServiceCategoryQuery {
    private final ServiceCategoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> listActive() {
        return repository.findAllByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(category -> new ServiceCategoryResponse(
                        category.getId(),
                        category.getSlug(),
                        category.getName(),
                        category.isActive(),
                        category.getDisplayOrder()
                ))
                .toList();
    }
}
