package com.cashly.cashly_api.infrastructure.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities.ProductEntity;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SeedReferencesTest {

    private final EntityManager entityManager = mock(EntityManager.class);

    @Test
    void returnsEntityWhenReferenceExists() {
        UUID id = UUID.randomUUID();
        ProductEntity product = mock(ProductEntity.class);
        when(entityManager.find(ProductEntity.class, id)).thenReturn(product);

        ProductEntity resolved = SeedReferences.findOrWarn(entityManager,
                ProductEntity.class, id, "product " + id);

        assertThat(resolved).isSameAs(product);
    }

    @Test
    void returnsNullWhenReferenceIsMissing() {
        UUID id = UUID.randomUUID();

        ProductEntity resolved = SeedReferences.findOrWarn(entityManager,
                ProductEntity.class, id, "product " + id);

        assertThat(resolved).isNull();
    }
}
