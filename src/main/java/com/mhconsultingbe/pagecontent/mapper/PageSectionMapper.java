package com.mhconsultingbe.pagecontent.mapper;

import com.mhconsultingbe.pagecontent.dto.response.PageSectionResponse;
import com.mhconsultingbe.pagecontent.entity.PageSection;

public final class PageSectionMapper {
    private PageSectionMapper() {}
    public static PageSectionResponse response(PageSection s) {
        return new PageSectionResponse(s.getId(), s.getPageKey(), s.getSectionKey(), s.getTitle(), s.getSubtitle(),
                s.getContent(), s.getImageUrl(), s.getButtonLabel(), s.getButtonUrl(), s.getDisplayOrder(),
                s.isActive(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
