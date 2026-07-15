package com.mhconsultingbe.consultation.service;

import com.mhconsultingbe.consultation.dto.ConsultationCreateRequest;
import com.mhconsultingbe.consultation.dto.ConsultationCreatedResponse;
import com.mhconsultingbe.consultation.dto.ConsultationResponse;
import com.mhconsultingbe.consultation.entity.ConsultationStatus;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

public interface ConsultationService {
    ConsultationCreatedResponse submit(ConsultationCreateRequest body);

    Page<ConsultationResponse> list(
            ConsultationStatus status,
            UUID serviceId,
            String phone,
            String email,
            Instant createdFrom,
            Instant createdTo,
            int page,
            int size,
            String[] sort
    );

    ConsultationResponse get(UUID id);

    ConsultationResponse status(UUID id, ConsultationStatus status);
}
