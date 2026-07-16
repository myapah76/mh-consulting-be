package com.mhconsultingbe.auth.repository;

import com.mhconsultingbe.auth.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
