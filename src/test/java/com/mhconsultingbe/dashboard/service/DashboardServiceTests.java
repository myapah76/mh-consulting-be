package com.mhconsultingbe.dashboard.service;

import com.mhconsultingbe.consultation.entity.ConsultationRequest;
import com.mhconsultingbe.consultation.entity.ConsultationStatus;
import com.mhconsultingbe.consultation.repository.ConsultationRequestRepository;
import com.mhconsultingbe.servicecatalog.entity.BusinessService;
import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.servicecatalog.repository.BusinessServiceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceTests {
    private final ConsultationRequestRepository consultationRepository =
            mock(ConsultationRequestRepository.class);
    private final BusinessServiceRepository businessServiceRepository =
            mock(BusinessServiceRepository.class);
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-07-17T01:00:00Z"),
            ZoneOffset.UTC
    );
    private final DashboardService service = new DashboardService(
            consultationRepository,
            businessServiceRepository,
            clock
    );

    @Test
    void defaultsToCurrentVietnameseWeekWithSevenDailyEntries() {
        when(consultationRepository
                .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
                        Instant.parse("2026-07-12T17:00:00Z"),
                        Instant.parse("2026-07-19T16:59:59.999Z")
                ))
                .thenReturn(List.of());

        var result = service.getDashboard(null, null);

        assertEquals(Instant.parse("2026-07-12T17:00:00Z"), result.period().from());
        assertEquals(Instant.parse("2026-07-19T16:59:59.999Z"), result.period().to());
        assertEquals("Asia/Ho_Chi_Minh", result.period().timezone());
        assertEquals(7, result.dailyConsultations().size());
        assertEquals(LocalDate.parse("2026-07-13"), result.dailyConsultations().get(0).date());
        assertEquals(LocalDate.parse("2026-07-19"), result.dailyConsultations().get(6).date());
        assertEquals(0.0, result.summary().completionRate());
    }

    @Test
    @SuppressWarnings("unchecked")
    void aggregatesDashboardDataUsingVietnameseLocalDates() {
        Instant from = Instant.parse("2026-07-12T17:00:00Z");
        Instant to = Instant.parse("2026-07-15T16:59:59.999Z");
        UUID accountingId = UUID.randomUUID();
        UUID taxId = UUID.randomUUID();
        List<ConsultationRequest> consultations = List.of(
                consultation(
                        "2026-07-12T17:30:00Z",
                        ConsultationStatus.NEW,
                        accountingId,
                        "Dịch vụ kế toán"
                ),
                consultation(
                        "2026-07-13T01:00:00Z",
                        ConsultationStatus.CONTACTED,
                        accountingId,
                        "Dịch vụ kế toán"
                ),
                consultation(
                        "2026-07-15T00:30:00Z",
                        ConsultationStatus.NEW,
                        accountingId,
                        "Dịch vụ kế toán"
                ),
                consultation(
                        "2026-07-15T01:00:00Z",
                        ConsultationStatus.COMPLETED,
                        taxId,
                        "Dịch vụ thuế"
                ),
                consultation(
                        "2026-07-15T02:00:00Z",
                        ConsultationStatus.COMPLETED,
                        taxId,
                        "Dịch vụ thuế"
                ),
                consultation(
                        "2026-07-15T03:00:00Z",
                        ConsultationStatus.CANCELLED,
                        null,
                        null
                )
        );
        when(consultationRepository
                .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(from, to))
                .thenReturn(consultations);
        when(businessServiceRepository.findAllByIdIn(anyCollection()))
                .thenReturn(List.of(
                        businessService(accountingId, "Kế toán"),
                        businessService(taxId, "Thuế")
                ));

        var result = service.getDashboard(from, to);

        assertEquals(6, result.summary().total());
        assertEquals(2, result.summary().newCount());
        assertEquals(1, result.summary().contactedCount());
        assertEquals(2, result.summary().completedCount());
        assertEquals(1, result.summary().cancelledCount());
        assertEquals(33.3, result.summary().completionRate());

        assertEquals(List.of(2L, 0L, 4L), result.dailyConsultations().stream()
                .map(item -> item.count())
                .toList());
        assertEquals(List.of("Dịch vụ kế toán", "Dịch vụ thuế"),
                result.consultationsByService().stream()
                        .map(item -> item.serviceTitle())
                        .toList());
        assertEquals(List.of(3L, 2L), result.consultationsByService().stream()
                .map(item -> item.count())
                .toList());
        assertEquals(List.of("Kế toán", "Thuế"), result.consultationsByCategory().stream()
                .map(item -> item.categoryName())
                .toList());
        assertEquals(5, result.recentConsultations().size());
        assertEquals(Instant.parse("2026-07-15T03:00:00Z"),
                result.recentConsultations().get(0).createdAt());

        ArgumentCaptor<java.util.Collection<UUID>> serviceIds = ArgumentCaptor.forClass(
                java.util.Collection.class
        );
        verify(businessServiceRepository).findAllByIdIn(serviceIds.capture());
        assertEquals(java.util.Set.of(accountingId, taxId), java.util.Set.copyOf(serviceIds.getValue()));
    }

    @Test
    void rejectsPartialOrReversedRanges() {
        Instant from = Instant.parse("2026-07-12T17:00:00Z");
        Instant to = Instant.parse("2026-07-19T16:59:59.999Z");

        assertThrows(IllegalArgumentException.class, () -> service.getDashboard(from, null));
        assertThrows(IllegalArgumentException.class, () -> service.getDashboard(null, to));
        assertThrows(IllegalArgumentException.class, () -> service.getDashboard(to, from));
    }

    private ConsultationRequest consultation(
            String createdAt,
            ConsultationStatus status,
            UUID serviceId,
            String serviceTitle
    ) {
        ConsultationRequest consultation = new ConsultationRequest();
        consultation.setId(UUID.randomUUID());
        consultation.setCustomerName("Nguyễn Văn A");
        consultation.setPhone("0912345678");
        consultation.setEmail("customer@example.com");
        consultation.setServiceId(serviceId);
        consultation.setServiceTitleSnapshot(serviceTitle);
        consultation.setStatus(status);
        consultation.setCreatedAt(Instant.parse(createdAt));
        return consultation;
    }

    private BusinessService businessService(UUID id, String categoryName) {
        ServiceCategory category = new ServiceCategory();
        category.setName(categoryName);
        BusinessService businessService = new BusinessService();
        businessService.setId(id);
        businessService.setCategory(category);
        return businessService;
    }
}
