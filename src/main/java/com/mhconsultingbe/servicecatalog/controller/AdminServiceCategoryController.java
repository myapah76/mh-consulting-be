package com.mhconsultingbe.servicecatalog.controller;

import com.mhconsultingbe.servicecatalog.dto.ActivePatchRequest;
import com.mhconsultingbe.servicecatalog.dto.AdminServiceCategoryResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceCategoryDeleteResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceCategoryUpsertRequest;
import com.mhconsultingbe.servicecatalog.service.ServiceCategoryOperations;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/service-categories")
@RequiredArgsConstructor
public class AdminServiceCategoryController {
    private final ServiceCategoryOperations service;

    @GetMapping
    Page<AdminServiceCategoryResponse> list(
            @RequestParam(required = false)
            Boolean active,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            @RequestParam(required = false)
            String sort
    ) {
        return service.list(active, page, size, sort);
    }

    @GetMapping("/{id}")
    AdminServiceCategoryResponse getById(
            @PathVariable
            UUID id
    ) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminServiceCategoryResponse create(
            @Valid
            @RequestBody
            ServiceCategoryUpsertRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    AdminServiceCategoryResponse update(
            @PathVariable
            UUID id,
            @Valid
            @RequestBody
            ServiceCategoryUpsertRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    AdminServiceCategoryResponse updateActive(
            @PathVariable
            UUID id,
            @Valid
            @RequestBody
            ActivePatchRequest request
    ) {
        return service.updateActive(id, request.active());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ServiceCategoryDeleteResponse> delete(
            @PathVariable
            UUID id
    ) {
        ServiceCategoryDeleteResponse result = service.delete(id);
        return result.deleted()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }
}
