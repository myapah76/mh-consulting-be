package com.mhconsultingbe.servicecatalog.repository;

import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID>, JpaSpecificationExecutor<ServiceCategory> {
    Optional<ServiceCategory> findBySlug(String slug);

    Optional<ServiceCategory> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<ServiceCategory> findAllByActiveTrueOrderByDisplayOrderAscNameAsc();
}
