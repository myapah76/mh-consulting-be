package com.mhconsultingbe.servicecatalog.mapper;

import com.mhconsultingbe.servicecatalog.dto.ServiceResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceSummaryResponse;
import com.mhconsultingbe.servicecatalog.entity.BusinessService;
import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.servicecatalog.entity.ServiceListItem;

public final class ServiceMapper {
    private ServiceMapper() {
    }

    public static ServiceSummaryResponse summary(BusinessService service) {
        ServiceCategory category = service.getCategory();
        return new ServiceSummaryResponse(
                service.getId(),
                service.getSlug(),
                service.getTitle(),
                category.getSlug(),
                category.getId(),
                category.getName(),
                service.getShortDescription(),
                service.getIcon(),
                service.isActive(),
                service.getDisplayOrder()
        );
    }

    public static ServiceResponse response(BusinessService service) {
        ServiceCategory category = service.getCategory();
        return new ServiceResponse(
                service.getId(),
                service.getSlug(),
                service.getTitle(),
                category.getSlug(),
                category.getId(),
                category.getName(),
                service.getShortDescription(),
                service.getIcon(),
                service.getFullContent(),
                service.isActive(),
                service.getDisplayOrder(),
                service.getDetailedPoints().stream().map(ServiceListItem::getContent).toList(),
                service.getBenefits().stream().map(ServiceListItem::getContent).toList(),
                service.getProcessSteps().stream().map(ServiceListItem::getContent).toList(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}
