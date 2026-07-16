package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.servicecatalog.repository.ServiceCategoryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceCategoryServiceTests {
    private final ServiceCategoryRepository repository = mock(ServiceCategoryRepository.class);
    private final ServiceCategoryService service = new ServiceCategoryService(repository);

    @Test
    void returnsRepositoryOrderedActiveCategoriesAsResponses() {
        ServiceCategory first = category("thanh-lap", "Thành lập doanh nghiệp", 0);
        ServiceCategory second = category("ke-toan", "Kế toán", 1);
        when(repository.findAllByActiveTrueOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(first, second));

        var result = service.listActive();

        assertEquals(List.of("thanh-lap", "ke-toan"),
                result.stream().map(response -> response.slug()).toList());
        assertEquals(List.of("Thành lập doanh nghiệp", "Kế toán"),
                result.stream().map(response -> response.name()).toList());
    }

    private ServiceCategory category(String slug, String name, int displayOrder) {
        ServiceCategory category = new ServiceCategory();
        category.setSlug(slug);
        category.setName(name);
        category.setDisplayOrder(displayOrder);
        return category;
    }
}
