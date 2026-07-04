package com.parsun.parsun_api.reports.inventory.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommercialCatalogResponse Unit Tests")
class CommercialCatalogResponseTest {

    @Test
    @DisplayName("should create commercial catalog response with all fields including price")
    void should_create_response_with_price() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1500.50");

        var response = new CommercialCatalogResponse(
                productId, "Product Name", "SKU-001", "A description",
                "products/photo.jpg", categoryId, "Audio", price, "Minorista");

        assertEquals(productId, response.productId());
        assertEquals("Product Name", response.productName());
        assertEquals("SKU-001", response.sku());
        assertEquals("A description", response.description());
        assertEquals("products/photo.jpg", response.photo());
        assertEquals(categoryId, response.categoryId());
        assertEquals("Audio", response.categoryName());
        assertEquals(price, response.price());
        assertEquals("Minorista", response.priceTypeName());
    }

    @Test
    @DisplayName("should create commercial catalog response with null photo, price, and priceTypeName")
    void should_create_response_with_nulls() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        var response = new CommercialCatalogResponse(
                productId, "Product", "SKU", "Desc",
                null, categoryId, "Category", null, null);

        assertNull(response.photo());
        assertNull(response.price());
        assertNull(response.priceTypeName());
    }
}
