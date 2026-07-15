package com.mhconsultingbe.consultation.controller;

import com.mhconsultingbe.consultation.dto.ConsultationCreateRequest;
import com.mhconsultingbe.consultation.dto.ConsultationCreatedResponse;
import com.mhconsultingbe.consultation.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/consultations")
@RequiredArgsConstructor
public class PublicConsultationController {
    private final ConsultationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ConsultationCreatedResponse submit(
            @Valid
            @RequestBody
            ConsultationCreateRequest body
    ) {
        return service.submit(body);
    }
}
