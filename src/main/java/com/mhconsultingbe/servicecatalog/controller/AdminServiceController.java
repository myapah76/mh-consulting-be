package com.mhconsultingbe.servicecatalog.controller;

import com.mhconsultingbe.servicecatalog.dto.request.ActivePatchRequest;
import com.mhconsultingbe.servicecatalog.dto.request.ServiceUpsertRequest;
import com.mhconsultingbe.servicecatalog.dto.response.ServiceResponse;
import com.mhconsultingbe.servicecatalog.dto.response.ServiceSummaryResponse;
import com.mhconsultingbe.servicecatalog.service.BusinessServiceOperations;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {
    private final BusinessServiceOperations service;

    @GetMapping
    Page<ServiceSummaryResponse> list(
            @RequestParam(required = false)
            String category,
            @RequestParam(required = false)
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

    @GetMapping("/{id}")
    ServiceResponse byId(
            @PathVariable
            UUID id
    ) {
        return service.byId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ServiceResponse create(
            @Valid
            @RequestBody
            ServiceUpsertRequest body
    ) {
        return service.create(body);
    }

    @PutMapping("/{id}")
    ServiceResponse update(
            @PathVariable
            UUID id,
            @Valid
            @RequestBody
            ServiceUpsertRequest body
    ) {
        return service.update(id, body);
    }

    @PatchMapping("/{id}/active")
    ServiceResponse active(
            @PathVariable
            UUID id,
            @Valid
            @RequestBody
            ActivePatchRequest body
    ) {
        return service.setActive(id, body.active());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(
            @PathVariable
            UUID id
    ) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(Map.of(
                        "deleted", false,
                        "active", false,
                        "message", "Referenced service was deactivated"
                ));
    }
}
