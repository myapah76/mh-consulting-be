package com.mhconsultingbe.consultation.service;

import com.mhconsultingbe.consultation.repository.ConsultationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationReferenceQueryService implements ConsultationReferenceQuery {
    private final ConsultationRequestRepository repository;

    @Override
    @Transactional(readOnly = true)
    public boolean isServiceReferenced(UUID serviceId) {
        return repository.existsByServiceId(serviceId);
    }
}
