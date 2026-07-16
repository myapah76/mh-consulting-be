package com.mhconsultingbe.servicecatalog.controller;

import com.mhconsultingbe.servicecatalog.dto.response.ServiceResponse;
import com.mhconsultingbe.servicecatalog.dto.response.ServiceSummaryResponse;
import com.mhconsultingbe.servicecatalog.service.BusinessServiceOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/services")
@RequiredArgsConstructor
public class PublicServiceController {
    private final BusinessServiceOperations service;

    @GetMapping
    Page<ServiceSummaryResponse> list(
            @RequestParam(required = false)
            String category,
            @RequestParam(defaultValue = "true")
            Boolean active,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            @RequestParam(required = false)
            String sort
    ) {
        return service.list(category, active, page, size, sort);
    }

    @GetMapping("/{slug}")
    ServiceResponse bySlug(
            @PathVariable
            String slug
    ) {
        return service.publicBySlug(slug);
    }
}
