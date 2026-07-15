package com.mhconsultingbe.consultation.controller;

import com.mhconsultingbe.consultation.dto.ConsultationResponse;
import com.mhconsultingbe.consultation.dto.ConsultationStatusRequest;
import com.mhconsultingbe.consultation.entity.ConsultationStatus;
import com.mhconsultingbe.consultation.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/consultations")
@RequiredArgsConstructor
public class AdminConsultationController {
    private final ConsultationService service;

    @GetMapping
    Page<ConsultationResponse> list(
            @RequestParam(required = false)
            ConsultationStatus status,
            @RequestParam(required = false)
            UUID serviceId,
            @RequestParam(required = false)
            String phone,
            @RequestParam(required = false)
            String email,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant createdTo,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            @RequestParam(required = false)
            String[] sort
    ) {
        return service.list(status, serviceId, phone, email, createdFrom, createdTo, page, size, sort);
    }

    @GetMapping("/{id}")
    ConsultationResponse get(
            @PathVariable
            UUID id
    ) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    ConsultationResponse status(
            @PathVariable
            UUID id,
            @Valid
            @RequestBody
            ConsultationStatusRequest body
    ) {
        return service.status(id, body.status());
    }
}
