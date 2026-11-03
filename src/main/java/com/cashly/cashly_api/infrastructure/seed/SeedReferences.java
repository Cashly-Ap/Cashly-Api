package com.cashly.cashly_api.infrastructure.seed;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SeedReferences {

    private static final Logger log = LoggerFactory
            .getLogger(SeedReferences.class);

    private SeedReferences() {
    }

    public static <T> T findOrWarn(EntityManager entityManager, Class<T> type,
            UUID id, String description) {
        T entity = entityManager.find(type, id);
        if (entity == null) {
            log.warn("Seed reference '{}' with id {} was not found; "
                    + "dependent row will be skipped", description, id);
        }
        return entity;
    }
}
