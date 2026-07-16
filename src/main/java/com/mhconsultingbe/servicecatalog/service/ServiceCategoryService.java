package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.servicecatalog.dto.request.ServiceCategoryUpsertRequest;
import com.mhconsultingbe.servicecatalog.dto.response.AdminServiceCategoryResponse;
import com.mhconsultingbe.servicecatalog.dto.response.ServiceCategoryDeleteResponse;
import com.mhconsultingbe.servicecatalog.dto.response.ServiceCategoryResponse;
import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.servicecatalog.repository.BusinessServiceRepository;
import com.mhconsultingbe.servicecatalog.repository.ServiceCategoryRepository;
import com.mhconsultingbe.shared.exception.ConflictException;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import com.mhconsultingbe.shared.util.PageableFactory;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService implements ServiceCategoryQuery, ServiceCategoryOperations {
    private static final Set<String> SORT_FIELDS = Set.of(
            "name",
            "slug",
            "active",
            "createdAt",
            "updatedAt"
    );

    private final ServiceCategoryRepository repository;
    private final BusinessServiceRepository businessServiceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> listActive() {
        return repository.findAllByActiveTrueOrderByNameAscIdAsc().stream()
                .map(category -> new ServiceCategoryResponse(
                        category.getId(),
                        category.getSlug(),
                        category.getName(),
                        category.isActive()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminServiceCategoryResponse> list(
            Boolean active,
            int page,
            int size,
            String sort
    ) {
        Specification<ServiceCategory> specification = active == null
                ? (root, query, builder) -> builder.conjunction()
                : (root, query, builder) -> builder.equal(root.get("active"), active);
        String[] sorting = TextNormalizer.trimToNull(sort) == null
                ? null
                : new String[]{sort.trim()};
        var pageable = PageableFactory.create(
                page,
                size,
                sorting,
                SORT_FIELDS,
                Sort.by(
                        Sort.Order.asc("name"),
                        Sort.Order.asc("id")
                )
        );
        return repository.findAll(specification, pageable).map(this::adminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminServiceCategoryResponse getById(UUID id) {
        return adminResponse(required(id));
    }

    @Override
    @Transactional
    public AdminServiceCategoryResponse create(ServiceCategoryUpsertRequest request) {
        String slug = TextNormalizer.lowercase(request.slug());
        if (repository.existsBySlug(slug)) {
            throw new ConflictException("DUPLICATE_SLUG", "Service category slug already exists");
        }
        ServiceCategory category = new ServiceCategory();
        apply(category, request, true);
        return adminResponse(repository.save(category));
    }

    @Override
    @Transactional
    public AdminServiceCategoryResponse update(UUID id, ServiceCategoryUpsertRequest request) {
        ServiceCategory category = required(id);
        String slug = TextNormalizer.lowercase(request.slug());
        if (repository.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("DUPLICATE_SLUG", "Service category slug already exists");
        }
        apply(category, request, false);
        return adminResponse(category);
    }

    @Override
    @Transactional
    public AdminServiceCategoryResponse updateActive(UUID id, boolean active) {
        ServiceCategory category = required(id);
        category.setActive(active);
        return adminResponse(category);
    }

    @Override
    @Transactional
    public ServiceCategoryDeleteResponse delete(UUID id) {
        ServiceCategory category = required(id);
        if (businessServiceRepository.existsByCategoryId(id)) {
            category.setActive(false);
            return new ServiceCategoryDeleteResponse(
                    false,
                    false,
                    "Referenced category was deactivated"
            );
        }
        repository.delete(category);
        return new ServiceCategoryDeleteResponse(true, false, "Category deleted");
    }

    private ServiceCategory required(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found"));
    }

    private void apply(
            ServiceCategory category,
            ServiceCategoryUpsertRequest request,
            boolean creating
    ) {
        category.setSlug(TextNormalizer.lowercase(request.slug()));
        category.setName(TextNormalizer.plainText(request.name()));
        if (request.active() != null) {
            category.setActive(request.active());
        } else if (creating) {
            category.setActive(true);
        }
    }

    private AdminServiceCategoryResponse adminResponse(ServiceCategory category) {
        return new AdminServiceCategoryResponse(
                category.getId(),
                category.getSlug(),
                category.getName(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
