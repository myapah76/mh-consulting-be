package com.mhconsultingbe.contact.repository;

import com.mhconsultingbe.contact.entity.ContactSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContactSettingsRepository extends JpaRepository<ContactSettings, UUID> {
    Optional<ContactSettings> findBySingletonKeyTrue();
}
