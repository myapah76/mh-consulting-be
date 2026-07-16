package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.consultation.service.ConsultationReferenceQuery;
import com.mhconsultingbe.servicecatalog.dto.ServiceUpsertRequest;
import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.servicecatalog.repository.BusinessServiceRepository;
import com.mhconsultingbe.servicecatalog.repository.ServiceCategoryRepository;
import com.mhconsultingbe.shared.exception.InvalidRequestException;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessServiceServiceTests {
    private final BusinessServiceRepository repository = mock(BusinessServiceRepository.class);
    private final ServiceCategoryRepository categoryRepository = mock(ServiceCategoryRepository.class);
    private final ConsultationReferenceQuery consultationReferences = mock(ConsultationReferenceQuery.class);
    private final BusinessServiceService service = new BusinessServiceService(
            repository,
            categoryRepository,
            consultationReferences
    );

    @Test
    void rejectsMissingCategoryWhenCreatingService() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(request(categoryId)));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsInactiveCategoryWhenCreatingService() {
        UUID categoryId = UUID.randomUUID();
        ServiceCategory category = new ServiceCategory();
        category.setActive(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThrows(InvalidRequestException.class, () -> service.create(request(categoryId)));
        verify(repository, never()).save(any());
    }

    private ServiceUpsertRequest request(UUID categoryId) {
        return new ServiceUpsertRequest(
                "service-slug",
                "Service title",
                categoryId,
                "Short description",
                null,
                null,
                true,
                0,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
