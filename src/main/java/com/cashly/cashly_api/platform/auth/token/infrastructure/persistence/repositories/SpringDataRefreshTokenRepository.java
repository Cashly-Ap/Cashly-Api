package com.cashly.cashly_api.platform.auth.token.infrastructure.persistence.repositories;

import com.cashly.cashly_api.platform.auth.token.infrastructure.persistence.entities.RefreshTokenEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRefreshTokenRepository
        extends
            JpaRepository<RefreshTokenEntity, UUID> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenEntity> findByToken(String token);

    @org.springframework.data.jpa.repository.Query("select r.subjectId from RefreshTokenEntity r where r.token = :token")
    Optional<UUID> findSubjectIdByToken(
            @org.springframework.data.repository.query.Param("token") String token);

    List<RefreshTokenEntity> findBySubjectId(UUID subjectId);

    List<RefreshTokenEntity> findBySubjectIdAndRevoked(UUID subjectId,
            boolean revoked);
}
