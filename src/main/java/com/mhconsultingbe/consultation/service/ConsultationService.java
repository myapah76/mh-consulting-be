package com.mhconsultingbe.consultation.service;

import com.mhconsultingbe.consultation.dto.*;
import com.mhconsultingbe.consultation.entity.*;
import com.mhconsultingbe.consultation.mail.ConsultationSubmittedEvent;
import com.mhconsultingbe.consultation.mapper.ConsultationMapper;
import com.mhconsultingbe.consultation.repository.ConsultationRequestRepository;
import com.mhconsultingbe.servicecatalog.service.ServiceCatalogQuery;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import com.mhconsultingbe.shared.util.PageableFactory;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class ConsultationService {
    private static final Set<String> SORT_FIELDS = Set.of("customerName", "phone", "email", "status", "createdAt", "updatedAt");
    private final ConsultationRequestRepository repository;
    private final ServiceCatalogQuery serviceCatalog;
    private final ApplicationEventPublisher events;

    @Transactional
    public ConsultationCreatedResponse submit(ConsultationCreateRequest body) {
        var request = new ConsultationRequest();
        request.setCustomerName(TextNormalizer.required(body.customerName()));
        request.setPhone(body.phone().trim().replaceAll("[\\s.-]", ""));
        request.setEmail(TextNormalizer.lowercase(body.email()));
        request.setMessage(TextNormalizer.plainText(body.message()));
        var selected = serviceCatalog.findActiveReference(body.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected active service not found"));
        request.setServiceId(selected.id());
        request.setServiceTitleSnapshot(selected.title());
        request = repository.save(request);
        events.publishEvent(new ConsultationSubmittedEvent(request.getId(), request.getCustomerName(), request.getPhone(),
                request.getEmail(), selected.categoryName(), request.getServiceTitleSnapshot(), request.getMessage(),
                request.getCreatedAt()));
        return new ConsultationCreatedResponse(request.getId(), request.getStatus().name(), "Consultation request received", request.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Page<ConsultationResponse> list(ConsultationStatus status, UUID serviceId, String phone, String email,
                                           Instant createdFrom, Instant createdTo, int page, int size, String[] sort) {
        Specification<ConsultationRequest> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) spec = spec.and((r,q,c) -> c.equal(r.get("status"), status));
        if (serviceId != null) spec = spec.and((r,q,c) -> c.equal(r.get("serviceId"), serviceId));
        if (phone != null && !phone.isBlank()) spec = spec.and((r,q,c) -> c.like(r.get("phone"), "%" + phone.trim() + "%"));
        if (email != null && !email.isBlank()) spec = spec.and((r,q,c) -> c.like(c.lower(r.get("email")), "%" + email.trim().toLowerCase() + "%"));
        if (createdFrom != null) spec = spec.and((r,q,c) -> c.greaterThanOrEqualTo(r.get("createdAt"), createdFrom));
        if (createdTo != null) spec = spec.and((r,q,c) -> c.lessThanOrEqualTo(r.get("createdAt"), createdTo));
        return repository.findAll(spec, PageableFactory.create(page, size, sort, SORT_FIELDS, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(ConsultationMapper::response);
    }
    @Transactional(readOnly = true)
    public ConsultationResponse get(UUID id) { return ConsultationMapper.response(required(id)); }
    @Transactional
    public ConsultationResponse status(UUID id, ConsultationStatus status) { var request = required(id); request.setStatus(status); return ConsultationMapper.response(request); }
    private ConsultationRequest required(UUID id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Consultation request not found")); }
}
