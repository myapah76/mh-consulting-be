package com.mhconsultingbe.pagecontent.controller;

import com.mhconsultingbe.pagecontent.dto.request.PageSectionRequest;
import com.mhconsultingbe.pagecontent.dto.response.PageSectionResponse;
import com.mhconsultingbe.pagecontent.service.PageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/admin/pages") @RequiredArgsConstructor
public class AdminPageController {
    private final PageContentService service;
    @GetMapping List<PageSectionResponse> all() { return service.all(); }
    @GetMapping("/{pageKey}") List<PageSectionResponse> page(@PathVariable String pageKey) { return service.adminPage(pageKey); }
    @PostMapping("/{pageKey}/sections") @ResponseStatus(HttpStatus.CREATED)
    PageSectionResponse create(@PathVariable String pageKey, @Valid @RequestBody PageSectionRequest body) { return service.create(pageKey, body); }
    @PutMapping("/{pageKey}/sections/{sectionId}")
    PageSectionResponse update(@PathVariable String pageKey, @PathVariable UUID sectionId, @Valid @RequestBody PageSectionRequest body) {
        return service.update(pageKey, sectionId, body);
    }
    @DeleteMapping("/{pageKey}/sections/{sectionId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable String pageKey, @PathVariable UUID sectionId) { service.delete(pageKey, sectionId); }
}
