package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.catalog.categories.infrastructure.persistence.entities.CategoryEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V07CategoriesSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 4,
            8, 0);
    private final EntityManager entityManager;
    public V07CategoriesSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 7;
    }
    @Override
    public void seed() {
        persist(CATEGORY_FOOD, "Food", "Packaged foods");
        persist(CATEGORY_DRINKS, "Drinks", "Hot and cold beverages");
    }
    private void persist(UUID id, String name, String description) {
        if (entityManager.find(CategoryEntity.class, id) != null)
            return;
        var entity = new CategoryEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(description);
        entity.setActive(true);
        entity.setCreatedAt(CREATED_AT);
        entity.setUpdatedAt(CREATED_AT);
        entityManager.persist(entity);
    }
}
