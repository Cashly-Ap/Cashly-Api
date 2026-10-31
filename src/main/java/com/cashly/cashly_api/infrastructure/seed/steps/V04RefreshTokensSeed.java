package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.token.infrastructure.persistence.entities.RefreshTokenEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V04RefreshTokensSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 1,
            9, 0);
    private final EntityManager entityManager;
    public V04RefreshTokensSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 4;
    }

    @Override
    public void seed() {
        persist(id("refresh-token.admin"), ADMIN_USER, "seed-refresh-admin");
        persist(id("refresh-token.operator"), OPERATOR_USER,
                "seed-refresh-operator");
    }

    private void persist(UUID id, UUID subjectId, String token) {
        if (entityManager.find(RefreshTokenEntity.class, id) != null)
            return;
        var entity = new RefreshTokenEntity();
        entity.setId(id);
        entity.setSubjectId(subjectId);
        entity.setSubjectType(AuthenticableType.USER);
        entity.setToken(token);
        entity.setExpiresAt(LocalDateTime.of(2035, 1, 1, 0, 0));
        entity.setRevoked(false);
        entity.setCreatedAt(CREATED_AT);
        entityManager.persist(entity);
    }
}
