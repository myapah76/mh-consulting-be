package com.mhconsultingbe.auth.repository;

import com.mhconsultingbe.auth.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from PasswordResetToken token join fetch token.admin where token.tokenHash = :tokenHash")
    Optional<PasswordResetToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    boolean existsByAdminIdAndCreatedAtAfter(UUID adminId, Instant createdAfter);

    @Modifying
    @Query("update PasswordResetToken token set token.usedAt = :usedAt "
            + "where token.admin.id = :adminId and token.usedAt is null")
    int markUnusedTokensUsed(@Param("adminId") UUID adminId, @Param("usedAt") Instant usedAt);

    @Modifying
    @Query("delete from PasswordResetToken token where token.expiresAt < :expiredBefore "
            + "or (token.usedAt is not null and token.usedAt < :usedBefore)")
    int deleteStaleTokens(
            @Param("expiredBefore") Instant expiredBefore,
            @Param("usedBefore") Instant usedBefore
    );
}
