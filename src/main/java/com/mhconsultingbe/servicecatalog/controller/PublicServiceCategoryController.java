package com.mhconsultingbe.servicecatalog.controller;

import com.mhconsultingbe.servicecatalog.dto.ServiceCategoryResponse;
import com.mhconsultingbe.servicecatalog.service.ServiceCategoryQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/service-categories")
@RequiredArgsConstructor
public class PublicServiceCategoryController {
    private final ServiceCategoryQuery service;

    @GetMapping
    List<ServiceCategoryResponse> list() {
        return service.listActive();
    }
}
