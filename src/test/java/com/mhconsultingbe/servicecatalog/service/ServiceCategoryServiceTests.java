package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.servicecatalog.dto.request.ServiceCategoryUpsertRequest;
import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.servicecatalog.repository.BusinessServiceRepository;
import com.mhconsultingbe.servicecatalog.repository.ServiceCategoryRepository;
import com.mhconsultingbe.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceCategoryServiceTests {
    private final ServiceCategoryRepository repository = mock(ServiceCategoryRepository.class);
    private final BusinessServiceRepository businessServiceRepository = mock(BusinessServiceRepository.class);
    private final ServiceCategoryService service = new ServiceCategoryService(
            repository,
            businessServiceRepository
    );

    @Test
    void returnsRepositoryOrderedActiveCategoriesAsPublicResponses() {
        ServiceCategory first = category("thanh-lap", "Thành lập doanh nghiệp", true);
        ServiceCategory second = category("ke-toan", "Kế toán", true);
        when(repository.findAllByActiveTrueOrderByNameAscIdAsc())
                .thenReturn(List.of(first, second));

        var result = service.listActive();

        assertEquals(List.of("thanh-lap", "ke-toan"),
                result.stream().map(response -> response.slug()).toList());
        verify(repository).findAllByActiveTrueOrderByNameAscIdAsc();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listsActiveAndInactiveWithDefaultSortingAndClampedSize() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        category("active", "Active", true),
                        category("inactive", "Inactive", false)
                )));

        var result = service.list(null, 0, 1000, null);

        assertEquals(2, result.getTotalElements());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
        assertEquals("name: ASC,id: ASC", pageable.getValue().getSort().toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void appliesActiveFilter() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(category("active", "Active", true))));

        var result = service.list(true, 0, 20, "name,desc");

        assertEquals(1, result.getTotalElements());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageable.capture());
        assertEquals("name: DESC", pageable.getValue().getSort().toString());
    }

    @Test
    void getsInactiveCategoryById() {
        ServiceCategory category = category("inactive", "Inactive", false);
        when(repository.findById(category.getId())).thenReturn(Optional.of(category));

        assertFalse(service.getById(category.getId()).active());
    }

    @Test
    void createsTrimmedCategoryWithDefaults() {
        when(repository.save(any(ServiceCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(request("new-category", "  New category  ", null));

        assertEquals("new-category", result.slug());
        assertEquals("New category", result.name());
        assertTrue(result.active());
    }

    @Test
    void rejectsDuplicateSlugOnCreate() {
        when(repository.existsBySlug("duplicate")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.create(request("duplicate", "Duplicate", true))
        );

        assertEquals("DUPLICATE_SLUG", exception.getCode());
        verify(repository, never()).save(any());
    }

    @Test
    void fullyUpdatesCategory() {
        ServiceCategory category = category("old", "Old", true);
        when(repository.findById(category.getId())).thenReturn(Optional.of(category));

        var result = service.update(
                category.getId(),
                request("updated", "Updated", false)
        );

        assertEquals("updated", result.slug());
        assertEquals("Updated", result.name());
        assertFalse(result.active());
    }

    @Test
    void rejectsDuplicateSlugOnUpdateExcludingCurrentId() {
        ServiceCategory category = category("old", "Old", true);
        when(repository.findById(category.getId())).thenReturn(Optional.of(category));
        when(repository.existsBySlugAndIdNot("duplicate", category.getId())).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.update(
                        category.getId(),
                        request("duplicate", "Updated", true)
                )
        );

        assertEquals("DUPLICATE_SLUG", exception.getCode());
    }

    @Test
    void activatesAndDeactivatesCategory() {
        ServiceCategory category = category("category", "Category", false);
        when(repository.findById(category.getId())).thenReturn(Optional.of(category));

        assertTrue(service.updateActive(category.getId(), true).active());
        assertFalse(service.updateActive(category.getId(), false).active());
    }

    @Test
    void deletesUnusedCategory() {
        ServiceCategory category = category("unused", "Unused", true);
        when(repository.findById(category.getId())).thenReturn(Optional.of(category));

        var result = service.delete(category.getId());

        assertTrue(result.deleted());
        verify(repository).delete(category);
    }

    @Test
    void deactivatesReferencedCategoryWithoutDeletingServices() {
        ServiceCategory category = category("referenced", "Referenced", true);
        when(repository.findById(category.getId())).thenReturn(Optional.of(category));
        when(businessServiceRepository.existsByCategoryId(category.getId())).thenReturn(true);

        var result = service.delete(category.getId());

        assertFalse(result.deleted());
        assertFalse(category.isActive());
        assertEquals("Referenced category was deactivated", result.message());
        verify(repository, never()).delete(any(ServiceCategory.class));
        verify(businessServiceRepository, never()).deleteAll();
    }

    private ServiceCategoryUpsertRequest request(
            String slug,
            String name,
            Boolean active
    ) {
        return new ServiceCategoryUpsertRequest(slug, name, active);
    }

    private ServiceCategory category(
            String slug,
            String name,
            boolean active
    ) {
        ServiceCategory category = new ServiceCategory();
        category.setId(UUID.randomUUID());
        category.setSlug(slug);
        category.setName(name);
        category.setActive(active);
        return category;
    }
}
