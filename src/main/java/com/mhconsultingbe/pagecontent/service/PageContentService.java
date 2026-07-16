package com.mhconsultingbe.pagecontent.service;

import com.mhconsultingbe.pagecontent.dto.*;
import com.mhconsultingbe.pagecontent.entity.PageSection;
import com.mhconsultingbe.pagecontent.mapper.PageSectionMapper;
import com.mhconsultingbe.pagecontent.repository.PageSectionRepository;
import com.mhconsultingbe.shared.exception.ConflictException;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class PageContentService {
    private final PageSectionRepository repository;

    @Transactional(readOnly = true)
    public List<PageSectionResponse> publicPage(String pageKey) {
        return repository.findByPageKeyAndActiveTrueOrderByDisplayOrderAsc(key(pageKey)).stream().map(PageSectionMapper::response).toList();
    }
    @Transactional(readOnly = true)
    public PageSectionResponse publicSection(String pageKey, String sectionKey) {
        return PageSectionMapper.response(repository.findByPageKeyAndSectionKeyAndActiveTrue(key(pageKey), key(sectionKey))
                .orElseThrow(() -> new ResourceNotFoundException("Page section not found")));
    }
    @Transactional(readOnly = true)
    public List<PageSectionResponse> all() { return repository.findAllByOrderByPageKeyAscDisplayOrderAsc().stream().map(PageSectionMapper::response).toList(); }
    @Transactional(readOnly = true)
    public List<PageSectionResponse> adminPage(String pageKey) { return repository.findByPageKeyOrderByDisplayOrderAsc(key(pageKey)).stream().map(PageSectionMapper::response).toList(); }

    @Transactional
    public PageSectionResponse create(String pageKey, PageSectionRequest body) {
        var page = key(pageKey); var section = key(body.sectionKey());
        if (repository.existsByPageKeyAndSectionKey(page, section)) throw duplicate();
        var entity = new PageSection(); entity.setPageKey(page); apply(entity, body, true);
        return PageSectionMapper.response(repository.save(entity));
    }
    @Transactional
    public PageSectionResponse update(String pageKey, UUID id, PageSectionRequest body) {
        var page = key(pageKey);
        var entity = repository.findByIdAndPageKey(id, page).orElseThrow(() -> new ResourceNotFoundException("Page section not found"));
        var section = key(body.sectionKey());
        if (repository.existsByPageKeyAndSectionKeyAndIdNot(page, section, id)) throw duplicate();
        apply(entity, body, false); return PageSectionMapper.response(entity);
    }
    @Transactional
    public void delete(String pageKey, UUID id) {
        repository.delete(repository.findByIdAndPageKey(id, key(pageKey)).orElseThrow(() -> new ResourceNotFoundException("Page section not found")));
    }
    private void apply(PageSection s, PageSectionRequest b, boolean creating) {
        s.setSectionKey(key(b.sectionKey())); s.setTitle(TextNormalizer.plainText(b.title()));
        s.setSubtitle(TextNormalizer.plainText(b.subtitle())); s.setContent(TextNormalizer.plainText(b.content()));
        s.setImageUrl(TextNormalizer.trimToNull(b.imageUrl())); s.setButtonLabel(TextNormalizer.plainText(b.buttonLabel()));
        s.setButtonUrl(TextNormalizer.trimToNull(b.buttonUrl())); s.setDisplayOrder(b.displayOrder() == null ? 0 : b.displayOrder());
        if (b.active() != null) s.setActive(b.active()); else if (creating) s.setActive(true);
    }
    private String key(String value) {
        var normalized = TextNormalizer.lowercase(value);
        if (normalized == null || !normalized.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw new com.mhconsultingbe.shared.exception.InvalidRequestException("Page and section keys must be lowercase URL-safe values");
        }
        return normalized;
    }
    private ConflictException duplicate() { return new ConflictException("DUPLICATE_PAGE_SECTION", "Section key already exists for this page"); }
}
