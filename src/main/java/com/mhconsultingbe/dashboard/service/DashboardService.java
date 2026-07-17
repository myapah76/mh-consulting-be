package com.mhconsultingbe.dashboard.service;

import com.mhconsultingbe.consultation.entity.ConsultationRequest;
import com.mhconsultingbe.consultation.entity.ConsultationStatus;
import com.mhconsultingbe.consultation.repository.ConsultationRequestRepository;
import com.mhconsultingbe.dashboard.dto.response.ConsultationByCategoryResponse;
import com.mhconsultingbe.dashboard.dto.response.ConsultationByServiceResponse;
import com.mhconsultingbe.dashboard.dto.response.DailyConsultationResponse;
import com.mhconsultingbe.dashboard.dto.response.DashboardPeriodResponse;
import com.mhconsultingbe.dashboard.dto.response.DashboardResponse;
import com.mhconsultingbe.dashboard.dto.response.DashboardSummaryResponse;
import com.mhconsultingbe.dashboard.dto.response.RecentConsultationResponse;
import com.mhconsultingbe.servicecatalog.entity.BusinessService;
import com.mhconsultingbe.servicecatalog.repository.BusinessServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService implements DashboardOperations {
    static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ConsultationRequestRepository consultationRepository;
    private final BusinessServiceRepository businessServiceRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Instant from, Instant to) {
        DateRange range = resolveRange(from, to);
        List<ConsultationRequest> consultations = consultationRepository
                .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(range.from(), range.to());
        Map<UUID, BusinessService> services = loadServices(consultations);

        return new DashboardResponse(
                new DashboardPeriodResponse(range.from(), range.to(), VIETNAM_ZONE.getId()),
                summary(consultations),
                dailyConsultations(consultations, range),
                consultationsByService(consultations),
                consultationsByCategory(consultations, services),
                recentConsultations(consultations)
        );
    }

    private DateRange resolveRange(Instant from, Instant to) {
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("Both from and to must be provided together");
        }
        if (from != null) {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("from must not be later than to");
            }
            return new DateRange(from, to);
        }

        LocalDate today = clock.instant().atZone(VIETNAM_ZONE).toLocalDate();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Instant weekFrom = monday.atStartOfDay(VIETNAM_ZONE).toInstant();
        Instant weekTo = monday.plusDays(7).atStartOfDay(VIETNAM_ZONE).toInstant().minusMillis(1);
        return new DateRange(weekFrom, weekTo);
    }

    private DashboardSummaryResponse summary(List<ConsultationRequest> consultations) {
        Map<ConsultationStatus, Long> counts = new EnumMap<>(ConsultationStatus.class);
        for (ConsultationRequest consultation : consultations) {
            counts.merge(consultation.getStatus(), 1L, Long::sum);
        }

        long newCount = counts.getOrDefault(ConsultationStatus.NEW, 0L);
        long contactedCount = counts.getOrDefault(ConsultationStatus.CONTACTED, 0L);
        long completedCount = counts.getOrDefault(ConsultationStatus.COMPLETED, 0L);
        long cancelledCount = counts.getOrDefault(ConsultationStatus.CANCELLED, 0L);
        long total = newCount + contactedCount + completedCount + cancelledCount;
        double completionRate = total == 0
                ? 0.0
                : Math.round(completedCount * 1000.0 / total) / 10.0;

        return new DashboardSummaryResponse(
                total,
                newCount,
                contactedCount,
                completedCount,
                cancelledCount,
                completionRate
        );
    }

    private List<DailyConsultationResponse> dailyConsultations(
            List<ConsultationRequest> consultations,
            DateRange range
    ) {
        LocalDate firstDate = range.from().atZone(VIETNAM_ZONE).toLocalDate();
        LocalDate lastDate = range.to().atZone(VIETNAM_ZONE).toLocalDate();
        Map<LocalDate, Long> counts = new HashMap<>();
        for (ConsultationRequest consultation : consultations) {
            LocalDate date = consultation.getCreatedAt().atZone(VIETNAM_ZONE).toLocalDate();
            counts.merge(date, 1L, Long::sum);
        }

        List<DailyConsultationResponse> daily = new ArrayList<>();
        for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            daily.add(new DailyConsultationResponse(date, counts.getOrDefault(date, 0L)));
        }
        return List.copyOf(daily);
    }

    private List<ConsultationByServiceResponse> consultationsByService(
            List<ConsultationRequest> consultations
    ) {
        Map<ServiceGroup, Long> counts = consultations.stream()
                .filter(consultation -> consultation.getServiceId() != null)
                .collect(Collectors.groupingBy(
                        consultation -> new ServiceGroup(
                                consultation.getServiceId(),
                                consultation.getServiceTitleSnapshot()
                        ),
                        Collectors.counting()
                ));

        Comparator<ConsultationByServiceResponse> order = Comparator
                .comparingLong(ConsultationByServiceResponse::count)
                .reversed()
                .thenComparing(
                        ConsultationByServiceResponse::serviceTitle,
                        Comparator.nullsLast(String::compareTo)
                );

        return counts.entrySet().stream()
                .map(entry -> new ConsultationByServiceResponse(
                        entry.getKey().serviceId(),
                        entry.getKey().serviceTitle(),
                        entry.getValue()
                ))
                .sorted(order)
                .limit(10)
                .toList();
    }

    private List<ConsultationByCategoryResponse> consultationsByCategory(
            List<ConsultationRequest> consultations,
            Map<UUID, BusinessService> services
    ) {
        Map<String, Long> counts = consultations.stream()
                .map(ConsultationRequest::getServiceId)
                .filter(Objects::nonNull)
                .map(services::get)
                .filter(Objects::nonNull)
                .map(BusinessService::getCategory)
                .filter(Objects::nonNull)
                .map(category -> category.getName())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Comparator<ConsultationByCategoryResponse> order = Comparator
                .comparingLong(ConsultationByCategoryResponse::count)
                .reversed()
                .thenComparing(ConsultationByCategoryResponse::categoryName);

        return counts.entrySet().stream()
                .map(entry -> new ConsultationByCategoryResponse(entry.getKey(), entry.getValue()))
                .sorted(order)
                .limit(10)
                .toList();
    }

    private List<RecentConsultationResponse> recentConsultations(
            List<ConsultationRequest> consultations
    ) {
        Comparator<ConsultationRequest> order = Comparator
                .comparing(ConsultationRequest::getCreatedAt)
                .reversed()
                .thenComparing(ConsultationRequest::getId);

        return consultations.stream()
                .sorted(order)
                .limit(5)
                .map(consultation -> new RecentConsultationResponse(
                        consultation.getId(),
                        consultation.getCustomerName(),
                        consultation.getPhone(),
                        consultation.getEmail(),
                        consultation.getServiceId(),
                        consultation.getServiceTitleSnapshot(),
                        consultation.getStatus(),
                        consultation.getCreatedAt()
                ))
                .toList();
    }

    private Map<UUID, BusinessService> loadServices(List<ConsultationRequest> consultations) {
        Set<UUID> serviceIds = consultations.stream()
                .map(ConsultationRequest::getServiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (serviceIds.isEmpty()) {
            return Map.of();
        }
        return businessServiceRepository.findAllByIdIn(serviceIds).stream()
                .collect(Collectors.toMap(BusinessService::getId, Function.identity()));
    }

    private record DateRange(Instant from, Instant to) {
    }

    private record ServiceGroup(UUID serviceId, String serviceTitle) {
    }
}
