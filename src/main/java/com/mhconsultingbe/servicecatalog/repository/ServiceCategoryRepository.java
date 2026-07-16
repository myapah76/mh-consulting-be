package com.mhconsultingbe.servicecatalog.repository;

import com.mhconsultingbe.servicecatalog.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {
    Optional<ServiceCategory> findBySlug(String slug);

    Optional<ServiceCategory> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);

    List<ServiceCategory> findAllByActiveTrueOrderByDisplayOrderAscNameAsc();
}
