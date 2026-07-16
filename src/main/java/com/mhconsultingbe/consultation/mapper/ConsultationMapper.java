package com.mhconsultingbe.consultation.mapper;

import com.mhconsultingbe.consultation.dto.response.ConsultationResponse;
import com.mhconsultingbe.consultation.entity.ConsultationRequest;

public final class ConsultationMapper {
    private ConsultationMapper() {}
    public static ConsultationResponse response(ConsultationRequest r) {
        return new ConsultationResponse(r.getId(), r.getCustomerName(), r.getPhone(), r.getEmail(), r.getServiceId(),
                r.getServiceTitleSnapshot(), r.getMessage(), r.getStatus(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
