package com.parsun.parsun_api.auth.token.infrastructure.persistence.repositories;

import com.parsun.parsun_api.auth.token.infrastructure.persistence.entities.RefreshTokenEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    List<RefreshTokenEntity> findBySubjectId(UUID subjectId);

    List<RefreshTokenEntity> findBySubjectIdAndRevoked(UUID subjectId, boolean revoked);
}
