package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities.ProductEntity;
import com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities.ProductPriceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V10ProductPricesSeed implements SeedStep {
    private final EntityManager entityManager;
    public V10ProductPricesSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 10; }
    @Override public void seed() {
        price("coffee-base", PRODUCT_COFFEE, "6.50", "44.44");
        price("water-base", PRODUCT_WATER, "1.00", "66.67");
        price("bundle-base", id("product.breakfast-bundle"), "8.00", "56.86");
    }
    private void price(String key, UUID productId, String amount, String margin) {
        UUID id = id("product-price." + key); if (entityManager.find(ProductPriceEntity.class, id) != null) return;
        var price = new ProductPriceEntity(); price.setId(id); price.setProduct(entityManager.getReference(ProductEntity.class, productId));
        price.setPriceTypeId(id("price-type.base")); price.setPrice(new BigDecimal(amount)); price.setProfitMargin(new BigDecimal(margin));
        entityManager.persist(price);
    }
}
