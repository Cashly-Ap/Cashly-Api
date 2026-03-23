package com.parsun.parsun_api.auth.token.infrastructure.persistence.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.token.infrastructure.persistence.entities.RefreshTokenEntity;


@Component
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    public JpaRefreshTokenRepository(SpringDataRefreshTokenRepository springDataRefreshTokenRepository) {
        this.springDataRefreshTokenRepository = springDataRefreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("RefreshToken cannot be null");
        }

        UUID tokenId = UUID.fromString(refreshToken.getId().getValue().toString());
        Optional<RefreshTokenEntity> existingEntity = springDataRefreshTokenRepository.findById(tokenId);

        RefreshTokenEntity entity;
        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            entity.updateFromDomain(refreshToken);
        } else {
            entity = RefreshTokenEntity.fromDomain(refreshToken);
        }

        RefreshTokenEntity savedEntity = springDataRefreshTokenRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        return springDataRefreshTokenRepository.findByToken(token)
            .map(RefreshTokenEntity::toDomain);
    }

    @Override
    public List<RefreshToken> findByUserId(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        UUID userUuid = UUID.fromString(userId.getValue().toString());
        return springDataRefreshTokenRepository.findBySubjectId(userUuid)
            .stream()
            .map(RefreshTokenEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        UUID userUuid = UUID.fromString(userId.getValue().toString());
        List<RefreshTokenEntity> tokens = springDataRefreshTokenRepository.findBySubjectIdAndRevoked(userUuid, false);
        for (RefreshTokenEntity tokenEntity : tokens) {
            RefreshToken token = tokenEntity.toDomain();
            token.revoke();
            tokenEntity.updateFromDomain(token);
            springDataRefreshTokenRepository.save(tokenEntity);
        }
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        List<RefreshTokenEntity> allTokens = springDataRefreshTokenRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (RefreshTokenEntity tokenEntity : allTokens) {
            RefreshToken token = tokenEntity.toDomain();
            if (token.isExpired()) {
                springDataRefreshTokenRepository.delete(tokenEntity);
            }
        }
    }
}
