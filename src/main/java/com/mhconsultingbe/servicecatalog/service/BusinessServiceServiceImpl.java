package com.mhconsultingbe.servicecatalog.service;

import com.mhconsultingbe.consultation.service.ConsultationReferenceQuery;
import com.mhconsultingbe.servicecatalog.dto.ServiceResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceSummaryResponse;
import com.mhconsultingbe.servicecatalog.dto.ServiceUpsertRequest;
import com.mhconsultingbe.servicecatalog.entity.BusinessService;
import com.mhconsultingbe.servicecatalog.entity.ServiceBenefit;
import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import com.mhconsultingbe.servicecatalog.entity.ServiceDetailPoint;
import com.mhconsultingbe.servicecatalog.entity.ServiceListItem;
import com.mhconsultingbe.servicecatalog.entity.ServiceProcessStep;
import com.mhconsultingbe.servicecatalog.mapper.ServiceMapper;
import com.mhconsultingbe.servicecatalog.repository.BusinessServiceRepository;
import com.mhconsultingbe.shared.exception.ConflictException;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import com.mhconsultingbe.shared.util.PageableFactory;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class BusinessServiceServiceImpl implements BusinessServiceService, ServiceCatalogQuery {
    private static final Set<String> SORT_FIELDS = Set.of("title", "slug", "category", "active", "displayOrder", "createdAt", "updatedAt");
    private final BusinessServiceRepository repository;
    private final ConsultationReferenceQuery consultationReferences;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceSummaryResponse> list(ServiceCategory category, Boolean active, int page, int size, String[] sort) {
        Specification<BusinessService> spec = (root, query, cb) -> cb.conjunction();
        if (category != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        if (active != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        var pageable = PageableFactory.create(page, size, sort, SORT_FIELDS,
                Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("title")));
        return repository.findAll(spec, pageable).map(ServiceMapper::summary);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse publicBySlug(String slug) {
        return ServiceMapper.response(repository.findBySlugAndActiveTrue(TextNormalizer.lowercase(slug))
                .orElseThrow(() -> new ResourceNotFoundException("Active service not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse byId(UUID id) {
        return ServiceMapper.response(required(id));
    }

    @Override
    @Transactional
    public ServiceResponse create(ServiceUpsertRequest body) {
        var slug = TextNormalizer.lowercase(body.slug());
        if (repository.existsBySlugIgnoreCase(slug)) throw new ConflictException("DUPLICATE_SLUG", "Service slug already exists");
        var service = new BusinessService();
        apply(service, body, true);
        return ServiceMapper.response(repository.save(service));
    }

    @Override
    @Transactional
    public ServiceResponse update(UUID id, ServiceUpsertRequest body) {
        var service = required(id);
        var slug = TextNormalizer.lowercase(body.slug());
        if (repository.existsBySlugIgnoreCaseAndIdNot(slug, id)) throw new ConflictException("DUPLICATE_SLUG", "Service slug already exists");
        apply(service, body, false);
        return ServiceMapper.response(service);
    }

    @Override
    @Transactional
    public ServiceResponse setActive(UUID id, boolean active) {
        var service = required(id);
        service.setActive(active);
        return ServiceMapper.response(service);
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        var service = required(id);
        if (consultationReferences.isServiceReferenced(id)) {
            service.setActive(false);
            return false;
        }
        repository.delete(service);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceReference> findActiveReference(UUID id) {
        return repository.findById(id).filter(BusinessService::isActive)
                .map(s -> new ServiceReference(s.getId(), s.getTitle()));
    }

    private BusinessService required(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    private void apply(BusinessService target, ServiceUpsertRequest body, boolean creating) {
        target.setSlug(TextNormalizer.lowercase(body.slug()));
        target.setTitle(TextNormalizer.plainText(body.title()));
        target.setCategory(body.category());
        target.setShortDescription(TextNormalizer.plainText(body.shortDesc()));
        target.setIcon(TextNormalizer.trimToNull(body.icon()));
        target.setFullContent(TextNormalizer.plainText(body.fullContent()));
        if (body.active() != null) target.setActive(body.active());
        else if (creating) target.setActive(true);
        target.setDisplayOrder(body.displayOrder() == null ? 0 : body.displayOrder());
        replace(target.getDetailedPoints(), body.detailedPoints(), ServiceDetailPoint::new);
        replace(target.getBenefits(), body.benefits(), ServiceBenefit::new);
        replace(target.getProcessSteps(), body.processSteps(), ServiceProcessStep::new);
    }

    private <T extends ServiceListItem> void replace(List<T> target, List<String> values, Supplier<T> factory) {
        target.clear();
        if (values == null) return;
        for (int i = 0; i < values.size(); i++) {
            var item = factory.get();
            item.setContent(TextNormalizer.plainText(values.get(i)));
            item.setDisplayOrder(i);
            target.add(item);
        }
    }
}
