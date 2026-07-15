package com.mhconsultingbe.pagecontent.repository;

import com.mhconsultingbe.pagecontent.entity.PageSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageSectionRepository extends JpaRepository<PageSection, UUID> {
    List<PageSection> findByPageKeyAndActiveTrueOrderByDisplayOrderAsc(String pageKey);
    List<PageSection> findByPageKeyOrderByDisplayOrderAsc(String pageKey);
    List<PageSection> findAllByOrderByPageKeyAscDisplayOrderAsc();
    Optional<PageSection> findByPageKeyAndSectionKeyAndActiveTrue(String pageKey, String sectionKey);
    boolean existsByPageKeyAndSectionKey(String pageKey, String sectionKey);
    boolean existsByPageKeyAndSectionKeyAndIdNot(String pageKey, String sectionKey, UUID id);
    Optional<PageSection> findByIdAndPageKey(UUID id, String pageKey);
}
