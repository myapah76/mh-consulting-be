package com.mhconsultingbe.servicecatalog.repository;

import com.mhconsultingbe.servicecatalog.entity.BusinessService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface BusinessServiceRepository extends JpaRepository<BusinessService, UUID>, JpaSpecificationExecutor<BusinessService> {
    boolean existsBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, UUID id);
    Optional<BusinessService> findBySlugAndActiveTrue(String slug);

    @EntityGraph(attributePaths = "category")
    Optional<BusinessService> findByIdAndActiveTrue(UUID id);
}
