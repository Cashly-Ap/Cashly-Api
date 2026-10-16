package com.cashly.cashly_api.products.infrastructure.integration;

import com.cashly.cashly_api.categories.domain.Category;
import com.cashly.cashly_api.categories.domain.CategoryRepository;
import com.cashly.cashly_api.categories.infrastructure.config.ProductCategoryQueryConfig;
import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.price_types.domain.PriceType;
import com.cashly.cashly_api.price_types.domain.PriceTypeRepository;
import com.cashly.cashly_api.price_types.infrastructure.config.ProductPriceTypeQueryConfig;
import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeNotFoundException;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductCatalogGatewayIntegrationTest {
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final PriceTypeRepository priceTypes = mock(PriceTypeRepository.class);
    private final LocalProductCategoryGatewayAdapter categoryGateway = new LocalProductCategoryGatewayAdapter(
            new ProductCategoryQueryConfig().productCategoryQuery(categories));
    private final LocalProductPriceTypeGatewayAdapter priceTypeGateway = new LocalProductPriceTypeGatewayAdapter(
            new ProductPriceTypeQueryConfig().productPriceTypeQuery(priceTypes));
    private final UUID id = UUID.randomUUID();

    @Test
    void shouldExposeExistingCategoryNamesWithoutFilteringHistoricalRecords() {
        Category category = mock(Category.class);
        when(category.getId()).thenReturn(id);
        when(category.getName()).thenReturn("Category");
        when(categories.findById(id)).thenReturn(Optional.of(category));
        var result = categoryGateway.require(id);
        assertEquals(id, result.id());
        assertEquals("Category", result.name());
    }

    @Test
    void shouldAllowMissingCategoryForReadAndRejectItForMutation() {
        assertTrue(categoryGateway.findById(id).isEmpty());
        assertThrows(CategoryNotFoundException.class, () -> categoryGateway.require(id));
    }

    @Test
    void shouldExposeExistingPriceTypeNamesWithoutForeignValueObjects() {
        PriceType priceType = mock(PriceType.class);
        when(priceType.getId()).thenReturn(id);
        when(priceType.getName()).thenReturn("Retail");
        when(priceTypes.findById(id)).thenReturn(Optional.of(priceType));
        var result = priceTypeGateway.require(id);
        assertEquals(id, result.id());
        assertEquals("Retail", result.name());
    }

    @Test
    void shouldAllowMissingPriceTypeForReadAndRejectItForMutation() {
        assertTrue(priceTypeGateway.findById(id).isEmpty());
        assertThrows(PriceTypeNotFoundException.class, () -> priceTypeGateway.require(id));
    }

    @Test
    void shouldPropagateCatalogLookupFailures() {
        IllegalStateException failure = new IllegalStateException("Lookup failed");
        when(categories.findById(id)).thenThrow(failure);
        when(priceTypes.findById(id)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> categoryGateway.findById(id)));
        assertSame(failure, assertThrows(IllegalStateException.class, () -> priceTypeGateway.findById(id)));
    }
}
