package com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.entities.ProductEntity;
import com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.entities.ProductPriceEntity;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ProductPriceMappingTest {
    @Test
    void shouldRoundTripPriceTypeIdAndAmountsWithoutForeignJpaEntity() {
        UUID priceId = UUID.randomUUID();
        UUID priceTypeId = UUID.randomUUID();
        ProductPrice price = ProductPrice.reconstitute(priceId, priceTypeId, new BigDecimal("12.3456"), new BigDecimal("15"));
        ProductPriceEntity entity = ProductPriceEntity.fromDomain(price, new ProductEntity());
        ProductPrice restored = entity.toDomain();
        assertEquals(priceId, restored.getId());
        assertEquals(priceTypeId, entity.getPriceTypeId());
        assertEquals(priceTypeId, restored.getPriceTypeId());
        assertEquals(price.getPrice(), restored.getPrice());
        assertEquals(price.getProfitMargin(), restored.getProfitMargin());
    }

    @Test
    void shouldKeepExistingBinaryColumnMapping() throws Exception {
        var field = ProductPriceEntity.class.getDeclaredField("priceTypeId");
        assertEquals(UUID.class, field.getType());
        Column mapping = field.getAnnotation(Column.class);
        assertEquals("price_type_id", mapping.name());
        assertEquals("BINARY(16)", mapping.columnDefinition());
        assertFalse(mapping.nullable());
        assertNull(field.getAnnotation(jakarta.persistence.ManyToOne.class));
    }

    @Test
    void shouldKeepOmittingPricesWhosePriceTypeNoLongerExists() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        ProductEntity entity = new ProductEntity();
        entity.applyPrices(List.of(ProductPrice.create(existingId, BigDecimal.TEN, null),
                ProductPrice.create(missingId, BigDecimal.ONE, null)), Set.of(existingId));
        assertEquals(1, entity.getProductPrices().size());
        assertEquals(existingId, entity.getProductPrices().iterator().next().getPriceTypeId());
    }

    @Test
    void shouldReplacePriceRowsOnUpdateWithoutChangingSelectionRules() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        Product product = Product.create("SKU", "Product", "Description", UUID.randomUUID(), false, BigDecimal.ONE);
        product.setProductPrices(List.of(ProductPrice.create(existingId, BigDecimal.TEN, null),
                ProductPrice.create(missingId, BigDecimal.ONE, null)));
        ProductEntity entity = ProductEntity.fromDomain(product);
        entity.applyPrices(List.of(ProductPrice.create(UUID.randomUUID(), BigDecimal.ONE, null)), Set.of());
        entity.updateFromDomain(product, Set.of(existingId));
        assertEquals(1, entity.getProductPrices().size());
        assertEquals(existingId, entity.toDomain().getProductPrices().getFirst().getPriceTypeId());
    }
}
