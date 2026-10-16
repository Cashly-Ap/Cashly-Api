package com.cashly.cashly_api.products.infrastructure.integration_tests;

import com.cashly.cashly_api.products.infrastructure.persistence.entities.ProductEntity;
import com.cashly.cashly_api.products.infrastructure.persistence.repositories.SpringDataProductRepository;
import com.cashly.cashly_api.products.infrastructure.persistence.repositories.SpringDataProductUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductPersistenceQueriesMySqlIT {
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Autowired
    private SpringDataProductRepository products;

    @Autowired
    private SpringDataProductUsageRepository productUsage;

    private final UUID categoryId = UUID.randomUUID();
    private final UUID otherCategoryId = UUID.randomUUID();
    private final UUID priceTypeId = UUID.randomUUID();
    private final UUID activeProductId = UUID.randomUUID();
    private final UUID componentId = UUID.randomUUID();
    private final UUID inactiveProductId = UUID.randomUUID();
    private final UUID otherCategoryProductId = UUID.randomUUID();
    private final UUID componentReferenceId = UUID.randomUUID();

    @BeforeEach
    void insertProductsAndReferences() {
        insertCategory(categoryId, "Products category");
        insertCategory(otherCategoryId, "Other category");
        insertPriceType(priceTypeId);
        insertProduct(activeProductId, "CASHLY-ACTIVE", "Active product", categoryId, false, true);
        insertProduct(componentId, "CASHLY-COMPONENT", "Component", categoryId, false, true);
        insertProduct(inactiveProductId, "CASHLY-INACTIVE", "Inactive product", categoryId, true, false);
        insertProduct(otherCategoryProductId, "CASHLY-OTHER", "Other category product", otherCategoryId, false, true);
        jdbc.update("INSERT INTO product_components (id, parent_product_id, component_product_id, quantity) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), ?)",
                componentReferenceId.toString(), activeProductId.toString(), componentId.toString(), 3);
        jdbc.update("INSERT INTO product_prices (id, product_id, price_type_id, price, profit_margin) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?)",
                UUID.randomUUID().toString(), activeProductId.toString(), priceTypeId.toString(), 12.5, 10.0);
    }

    @Test
    void shouldExecuteProductFilterComponentReferenceAndCatalogUsageQueriesAgainstMysql() {
        var filtered = products.findAllWithFilters("active", categoryId, true, false, PageRequest.of(0, 1));
        var activeProducts = products.findAllForOrder(null, categoryId, null, PageRequest.of(0, 10));
        var withStock = products.findAllForOrderWithStock(null, categoryId, null, Set.of(activeProductId), PageRequest.of(0, 10));
        var withoutStock = products.findAllForOrderWithoutStock(null, categoryId, null, Set.of(activeProductId), PageRequest.of(0, 10));
        ProductEntity productWithComponents = products.findWithComponentsById(activeProductId).orElseThrow();

        assertThat(filtered.getContent()).extracting(ProductEntity::getId).containsExactly(activeProductId);
        assertThat(filtered.getTotalElements()).isEqualTo(1);
        assertThat(activeProducts.getContent()).extracting(ProductEntity::getId)
                .containsExactlyInAnyOrder(activeProductId, componentId);
        assertThat(withStock.getContent()).extracting(ProductEntity::getId).containsExactly(activeProductId);
        assertThat(withoutStock.getContent()).extracting(ProductEntity::getId).containsExactly(componentId);
        assertThat(productWithComponents.getComponents()).hasSize(1);
        assertThat(products.findProductIdsWithComponentReferences(Set.of(componentId))).containsExactly(componentId);
        assertThat(productUsage.countActiveProductsByCategory(categoryId)).isEqualTo(2);
        assertThat(productUsage.countActiveProductsByCategoryIds(Set.of(categoryId, otherCategoryId)))
                .containsExactlyInAnyOrder(new Object[]{categoryId, 2L}, new Object[]{otherCategoryId, 1L});
        assertThat(productUsage.isInUseByProducts(priceTypeId)).isTrue();
        assertThat(productUsage.findIdsInUseByProducts(Set.of(priceTypeId))).containsExactly(priceTypeId);
    }

    @Test
    void shouldDeleteOnlyComponentRowsForTheRequestedComponent() {
        products.deleteByComponentProductId(componentId);

        assertThat(products.findWithComponentsById(activeProductId).orElseThrow().getComponents()).isEmpty();
        assertThat(products.findProductIdsWithComponentReferences(Set.of(componentId))).isEmpty();
    }

    private void insertCategory(UUID id, String name) {
        jdbc.update("INSERT INTO categories (id, name, active, created_at, updated_at) VALUES (UUID_TO_BIN(?), ?, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                id.toString(), name);
    }

    private void insertPriceType(UUID id) {
        jdbc.update("INSERT INTO price_types (id, name, active, created_at, updated_at) VALUES (UUID_TO_BIN(?), ?, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                id.toString(), "Price type " + id);
    }

    private void insertProduct(UUID id, String code, String name, UUID categoryId, boolean composite, boolean active) {
        jdbc.update("INSERT INTO products (id, code, name, category_id, composite, active, created_at, updated_at) VALUES (UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?), ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                id.toString(), code, name, categoryId.toString(), composite, active);
    }
}
