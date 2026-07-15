package com.mhconsultingbe.pagecontent.controller;

import com.mhconsultingbe.pagecontent.dto.PageSectionRequest;
import com.mhconsultingbe.pagecontent.dto.PageSectionResponse;
import com.mhconsultingbe.pagecontent.service.PageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/pages")
@RequiredArgsConstructor
public class AdminPageController {
    private final PageContentService service;

    @GetMapping
    List<PageSectionResponse> all() {
        return service.all();
    }

    @GetMapping("/{pageKey}")
    List<PageSectionResponse> page(
            @PathVariable
            String pageKey
    ) {
        return service.adminPage(pageKey);
    }

    @PostMapping("/{pageKey}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    PageSectionResponse create(
            @PathVariable
            String pageKey,
            @Valid
            @RequestBody
            PageSectionRequest body
    ) {
        return service.create(pageKey, body);
    }

    @PutMapping("/{pageKey}/sections/{sectionId}")
    PageSectionResponse update(
            @PathVariable
            String pageKey,
            @PathVariable
            UUID sectionId,
            @Valid
            @RequestBody
            PageSectionRequest body
    ) {
        return service.update(pageKey, sectionId, body);
    }

    @DeleteMapping("/{pageKey}/sections/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(
            @PathVariable
            String pageKey,
            @PathVariable
            UUID sectionId
    ) {
        service.delete(pageKey, sectionId);
    }
}
