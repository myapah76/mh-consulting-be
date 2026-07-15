package com.mhconsultingbe.pagecontent.service;

import com.mhconsultingbe.pagecontent.dto.PageSectionRequest;
import com.mhconsultingbe.pagecontent.dto.PageSectionResponse;

import java.util.List;
import java.util.UUID;

public interface PageContentService {
    List<PageSectionResponse> publicPage(String pageKey);

    PageSectionResponse publicSection(String pageKey, String sectionKey);

    List<PageSectionResponse> all();

    List<PageSectionResponse> adminPage(String pageKey);

    PageSectionResponse create(String pageKey, PageSectionRequest body);

    PageSectionResponse update(String pageKey, UUID id, PageSectionRequest body);

    void delete(String pageKey, UUID id);
}
