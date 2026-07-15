package com.mhconsultingbe.servicecatalog.mapper;

import com.mhconsultingbe.servicecatalog.dto.ServiceResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceSummaryResponse;
import com.mhconsultingbe.servicecatalog.entity.BusinessService;
import com.mhconsultingbe.servicecatalog.entity.ServiceListItem;

public final class ServiceMapper {
    private ServiceMapper() {}
    public static ServiceSummaryResponse summary(BusinessService s) {
        return new ServiceSummaryResponse(s.getId(), s.getSlug(), s.getTitle(), s.getCategory(),
                s.getShortDescription(), s.getIcon(), s.isActive(), s.getDisplayOrder());
    }
    public static ServiceResponse response(BusinessService s) {
        return new ServiceResponse(s.getId(), s.getSlug(), s.getTitle(), s.getCategory(), s.getShortDescription(),
                s.getIcon(), s.getFullContent(), s.isActive(), s.getDisplayOrder(),
                s.getDetailedPoints().stream().map(ServiceListItem::getContent).toList(),
                s.getBenefits().stream().map(ServiceListItem::getContent).toList(),
                s.getProcessSteps().stream().map(ServiceListItem::getContent).toList(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
