package com.mhconsultingbe.consultation.service;

import java.util.UUID;

public interface ConsultationReferenceQuery {
    boolean isServiceReferenced(UUID serviceId);
}
