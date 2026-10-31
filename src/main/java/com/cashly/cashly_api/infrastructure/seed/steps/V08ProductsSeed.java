package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities.ProductComponentEntity;
import com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities.ProductEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V08ProductsSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 5, 8, 0);
    private final EntityManager entityManager;
    public V08ProductsSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 8; }

    @Override public void seed() {
        persistSimple(PRODUCT_COFFEE, "PRD-0001", "Ground coffee", CATEGORY_DRINKS, "4.50");
        persistSimple(PRODUCT_WATER, "PRD-0002", "Mineral water", CATEGORY_DRINKS, "0.60");
        UUID bundleId = id("product.breakfast-bundle");
        if (entityManager.find(ProductEntity.class, bundleId) == null) {
            var bundle = product(bundleId, "PRD-0003", "Breakfast bundle", CATEGORY_FOOD, "5.10", true);
            addComponent(bundle, PRODUCT_COFFEE, 1, "coffee");
            addComponent(bundle, PRODUCT_WATER, 2, "water");
            entityManager.persist(bundle);
        }
    }

    private void persistSimple(UUID id, String code, String name, UUID categoryId, String purchasePrice) {
        if (entityManager.find(ProductEntity.class, id) == null)
            entityManager.persist(product(id, code, name, categoryId, purchasePrice, false));
    }

    private static ProductEntity product(UUID id, String code, String name, UUID categoryId, String price, boolean composite) {
        var product = new ProductEntity(); product.setId(id); product.setCode(code); product.setName(name);
        product.setDescription("Deterministic seed product"); product.setCategoryId(categoryId); product.setComposite(composite);
        product.setActive(true); product.setPurchasePrice(new BigDecimal(price)); product.setCreatedAt(CREATED_AT); product.setUpdatedAt(CREATED_AT);
        return product;
    }

    private static void addComponent(ProductEntity parent, UUID componentId, int quantity, String key) {
        var component = new ProductComponentEntity(); component.setId(id("product-component." + key));
        component.setProduct(parent); component.setComponentProductId(componentId); component.setQuantity(quantity);
        parent.getComponents().add(component);
    }
}
