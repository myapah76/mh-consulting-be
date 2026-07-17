package com.mhconsultingbe.consultation.repository;

import com.mhconsultingbe.consultation.entity.ConsultationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, UUID>, JpaSpecificationExecutor<ConsultationRequest> {
    boolean existsByServiceId(UUID serviceId);

    List<ConsultationRequest> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
            Instant from,
            Instant to
    );
}
