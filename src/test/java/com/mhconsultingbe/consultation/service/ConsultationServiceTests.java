package com.mhconsultingbe.consultation.service;

import com.mhconsultingbe.consultation.dto.request.ConsultationCreateRequest;
import com.mhconsultingbe.consultation.entity.ConsultationRequest;
import com.mhconsultingbe.consultation.mail.ConsultationSubmittedEvent;
import com.mhconsultingbe.consultation.repository.ConsultationRequestRepository;
import com.mhconsultingbe.servicecatalog.service.ServiceCatalogQuery;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultationServiceTests {
    private final ConsultationRequestRepository repository = mock(ConsultationRequestRepository.class);
    private final ServiceCatalogQuery serviceCatalog = mock(ServiceCatalogQuery.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final ConsultationService service = new ConsultationService(repository, serviceCatalog, events);

    @Test
    void derivesCategoryFromSelectedServiceForNotification() {
        UUID serviceId = UUID.randomUUID();
        var selected = new ServiceCatalogQuery.ServiceReference(
                serviceId,
                "Dịch Vụ Kế Toán Trọn Gói",
                "Kế toán"
        );
        when(serviceCatalog.findActiveReference(serviceId)).thenReturn(Optional.of(selected));
        when(repository.save(any(ConsultationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.submit(request(serviceId));

        var requestCaptor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(repository).save(requestCaptor.capture());
        assertEquals(serviceId, requestCaptor.getValue().getServiceId());
        assertEquals(selected.title(), requestCaptor.getValue().getServiceTitleSnapshot());

        var eventCaptor = ArgumentCaptor.forClass(ConsultationSubmittedEvent.class);
        verify(events).publishEvent(eventCaptor.capture());
        assertEquals("Kế toán", eventCaptor.getValue().serviceCategoryName());
        assertEquals(selected.title(), eventCaptor.getValue().serviceTitle());
    }

    @Test
    void rejectsUnknownOrInactiveSelectedService() {
        UUID serviceId = UUID.randomUUID();
        when(serviceCatalog.findActiveReference(serviceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.submit(request(serviceId)));

        verify(repository, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    private ConsultationCreateRequest request(UUID serviceId) {
        return new ConsultationCreateRequest(
                "Nguyễn Văn A",
                "0912 345 678",
                "EMAIL@example.com",
                serviceId,
                "Nội dung cần tư vấn"
        );
    }
}
