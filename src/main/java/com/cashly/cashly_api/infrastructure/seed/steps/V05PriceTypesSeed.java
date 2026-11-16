package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.id;

import com.cashly.cashly_api.catalog.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V05PriceTypesSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 2,
            8, 0);
    private final EntityManager entityManager;
    public V05PriceTypesSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 5;
    }
    @Override
    public void seed() {
        persist(id("price-type.base"), "Base", "Default retail price");
        persist(id("price-type.wholesale"), "Wholesale", "Volume sales price");
        persist(id("price-type.special"), "Special",
                "Preferred customer price");
    }
    private void persist(UUID id, String name, String description) {
        if (entityManager.find(PriceTypeEntity.class, id) != null)
            return;
        var entity = new PriceTypeEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(description);
        entity.setActive(true);
        entity.setCreatedAt(CREATED_AT);
        entity.setUpdatedAt(CREATED_AT);
        entityManager.persist(entity);
    }
}
