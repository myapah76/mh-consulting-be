package com.mhconsultingbe.pagecontent.controller;

import com.mhconsultingbe.pagecontent.dto.PageSectionResponse;
import com.mhconsultingbe.pagecontent.service.PageContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/pages")
@RequiredArgsConstructor
public class PublicPageController {
    private final PageContentService service;

    @GetMapping("/{pageKey}")
    List<PageSectionResponse> page(
            @PathVariable
            String pageKey
    ) {
        return service.publicPage(pageKey);
    }

    @GetMapping("/{pageKey}/sections/{sectionKey}")
    PageSectionResponse section(
            @PathVariable
            String pageKey,
            @PathVariable
            String sectionKey
    ) {
        return service.publicSection(pageKey, sectionKey);
    }
}
