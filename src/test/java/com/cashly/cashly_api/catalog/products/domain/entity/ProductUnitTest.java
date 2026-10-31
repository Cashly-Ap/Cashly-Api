package com.cashly.cashly_api.catalog.products.domain.entity;

import com.cashly.cashly_api.catalog.products.domain.valueobjects.ProductPrice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Domain Entity Unit Tests")
class ProductUnitTest {

    private static final String VALID_CODE = "PROD-001";
    private static final String VALID_NAME = "Test Product";
    private static final String VALID_DESCRIPTION = "A test product";
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Test
    void should_CreateActiveProduct_When_ValidData() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, new BigDecimal("5.00"));

        assertNull(product.getId());
        assertEquals("PROD-001", product.getCode());
        assertEquals("Test Product", product.getName());
        assertEquals(VALID_DESCRIPTION, product.getDescription());
        assertEquals(CATEGORY_ID, product.getCategoryId());
        assertFalse(product.isComposite());
        assertTrue(product.isActive());
        assertNull(product.getPhoto());
        assertEquals(new BigDecimal("5.00"), product.getPurchasePrice());
        assertTrue(product.getComponents().isEmpty());
        assertTrue(product.getProductPrices().isEmpty());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    void should_CreateCompositeProduct_When_CompositeFlagTrue() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, true, null);

        assertTrue(product.isComposite());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<ProductComponent> components = List.of(new ProductComponent(
                UUID.randomUUID(), id, UUID.randomUUID(), 2));
        List<ProductPrice> prices = List
                .of(ProductPrice.reconstitute(UUID.randomUUID(),
                        UUID.randomUUID(), new BigDecimal("10.00"), null));

        Product product = Product.reconstitute(id, VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, true, false, "photos/test.jpg",
                new BigDecimal("5.00"), components, prices, now, now);

        assertEquals(id, product.getId());
        assertEquals("PROD-001", product.getCode());
        assertEquals("Test Product", product.getName());
        assertTrue(product.isComposite());
        assertFalse(product.isActive());
        assertEquals("photos/test.jpg", product.getPhoto());
        assertEquals(new BigDecimal("5.00"), product.getPurchasePrice());
        assertEquals(1, product.getComponents().size());
        assertEquals(1, product.getProductPrices().size());
        assertEquals(now, product.getCreatedAt());
        assertEquals(now, product.getUpdatedAt());
    }

    @Test
    void should_UpdateFields_When_ValidData() throws InterruptedException {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        Thread.sleep(10);
        product.update("Updated Name", "Updated desc", UUID.randomUUID(),
                new BigDecimal("10.00"), true);

        assertEquals("Updated Name", product.getName());
        assertEquals("Updated desc", product.getDescription());
        assertEquals(new BigDecimal("10.00"), product.getPurchasePrice());
        assertTrue(product.isActive());
        assertTrue(product.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void should_NotChangeActive_When_ActiveIsNull() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        product.deactivate();

        product.update("Updated Name", "Updated desc", CATEGORY_ID, null, null);

        assertFalse(product.isActive());
    }

    @Test
    void should_ThrowException_When_UpdateNameIsNull() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);

        assertThrows(IllegalArgumentException.class,
                () -> product.update(null, "desc", CATEGORY_ID, null, null));
    }

    @Test
    void should_ThrowException_When_UpdateNameIsBlank() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);

        assertThrows(IllegalArgumentException.class,
                () -> product.update("   ", "desc", CATEGORY_ID, null, null));
    }

    @Test
    void should_ThrowException_When_UpdateNameOver200Chars() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        String longName = "a".repeat(201);

        assertThrows(IllegalArgumentException.class, () -> product
                .update(longName, "desc", CATEGORY_ID, null, null));
    }

    @Test
    void should_Activate_When_Deactivated() throws InterruptedException {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        product.deactivate();
        LocalDateTime beforeActivate = product.getUpdatedAt();

        Thread.sleep(10);
        product.activate();

        assertTrue(product.isActive());
        assertTrue(product.getUpdatedAt().isAfter(beforeActivate));
    }

    @Test
    void should_Deactivate_When_Active() throws InterruptedException {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        LocalDateTime beforeDeactivate = product.getUpdatedAt();

        Thread.sleep(10);
        product.deactivate();

        assertFalse(product.isActive());
        assertTrue(product.getUpdatedAt().isAfter(beforeDeactivate));
    }

    @Test
    void should_MakeComposite_When_NotComposite() throws InterruptedException {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        LocalDateTime beforeMake = product.getUpdatedAt();

        Thread.sleep(10);
        product.makeComposite();

        assertTrue(product.isComposite());
        assertTrue(product.getUpdatedAt().isAfter(beforeMake));
    }

    @Test
    void should_ThrowException_When_SetComponentsOnNonComposite() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> product.setComponents(List.of()));

        assertEquals("Cannot set components on a non-composite product",
                exception.getMessage());
    }

    @Test
    void should_SetComponents_When_Composite() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, true, null);
        UUID compId = UUID.randomUUID();
        List<ProductComponent> components = List.of(new ProductComponent(
                UUID.randomUUID(), product.getId(), compId, 3));

        product.setComponents(components);

        assertEquals(1, product.getComponents().size());
        assertEquals(compId,
                product.getComponents().get(0).getComponentProductId());
    }

    @Test
    void should_SetProductPrices_When_ValidList() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        UUID priceTypeId = UUID.randomUUID();
        List<ProductPrice> prices = List.of(ProductPrice.create(priceTypeId,
                new BigDecimal("25.00"), null));

        product.setProductPrices(prices);

        assertEquals(1, product.getProductPrices().size());
        assertEquals(priceTypeId,
                product.getProductPrices().get(0).getPriceTypeId());
    }

    @Test
    void should_AssignPhoto_When_PathProvided() throws InterruptedException {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        assertNull(product.getPhoto());
        LocalDateTime beforeAssign = product.getUpdatedAt();

        Thread.sleep(10);
        product.assignPhoto("products/test.jpg");

        assertEquals("products/test.jpg", product.getPhoto());
        assertTrue(product.getUpdatedAt().isAfter(beforeAssign));
    }

    @Test
    void should_RemovePhoto_When_PhotoExists() throws InterruptedException {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);
        product.assignPhoto("products/test.jpg");
        LocalDateTime beforeRemove = product.getUpdatedAt();

        Thread.sleep(10);
        product.removePhoto();

        assertNull(product.getPhoto());
        assertTrue(product.getUpdatedAt().isAfter(beforeRemove));
    }

    @Test
    void should_ThrowException_When_CodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> Product.create(null,
                VALID_NAME, VALID_DESCRIPTION, CATEGORY_ID, false, null));
    }

    @Test
    void should_ThrowException_When_CodeIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> Product.create("   ",
                VALID_NAME, VALID_DESCRIPTION, CATEGORY_ID, false, null));
    }

    @Test
    void should_ThrowException_When_CodeOver50Chars() {
        String longCode = "A".repeat(51);

        assertThrows(IllegalArgumentException.class,
                () -> Product.create(longCode, VALID_NAME, VALID_DESCRIPTION,
                        CATEGORY_ID, false, null));
    }

    @Test
    void should_ThrowException_When_NameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> Product.create(VALID_CODE, null, VALID_DESCRIPTION,
                        CATEGORY_ID, false, null));
    }

    @Test
    void should_ThrowException_When_NameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> Product.create(VALID_CODE, "   ", VALID_DESCRIPTION,
                        CATEGORY_ID, false, null));
    }

    @Test
    void should_ThrowException_When_NameOver200Chars() {
        String longName = "a".repeat(201);

        assertThrows(IllegalArgumentException.class,
                () -> Product.create(VALID_CODE, longName, VALID_DESCRIPTION,
                        CATEGORY_ID, false, null));
    }

    @Test
    void should_TrimAndUppercaseCode_When_CreatedWithSpacesAndLowercase() {
        Product product = Product.create("  prod-001  ", VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);

        assertEquals("PROD-001", product.getCode());
    }

    @Test
    void should_TrimName_When_CreatedWithSpaces() {
        Product product = Product.create(VALID_CODE, "  Test Product  ",
                VALID_DESCRIPTION, CATEGORY_ID, false, null);

        assertEquals("Test Product", product.getName());
    }

    @Test
    void should_ReturnUnmodifiableComponents_When_GetComponentsCalled() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, true, null);

        assertThrows(UnsupportedOperationException.class,
                () -> product.getComponents()
                        .add(new ProductComponent(UUID.randomUUID(),
                                product.getId(), UUID.randomUUID(), 1)));
    }

    @Test
    void should_ReturnUnmodifiableProductPrices_When_GetProductPricesCalled() {
        Product product = Product.create(VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, CATEGORY_ID, false, null);

        assertThrows(UnsupportedOperationException.class,
                () -> product.getProductPrices().add(ProductPrice
                        .create(UUID.randomUUID(), BigDecimal.TEN, null)));
    }

    @Test
    void should_BeEqual_When_SameId() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Product p1 = Product.reconstitute(id, "CODE-1", "Name 1", "Desc 1",
                CATEGORY_ID, false, true, null, null, List.of(), List.of(), now,
                now);
        Product p2 = Product.reconstitute(id, "CODE-2", "Name 2", "Desc 2",
                UUID.randomUUID(), true, false, "photo.jpg",
                new BigDecimal("5"), List.of(), List.of(), now, now);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIds() {
        LocalDateTime now = LocalDateTime.now();
        Product p1 = Product.reconstitute(UUID.randomUUID(), VALID_CODE,
                VALID_NAME, VALID_DESCRIPTION, CATEGORY_ID, false, true, null,
                null, List.of(), List.of(), now, now);
        Product p2 = Product.reconstitute(UUID.randomUUID(), VALID_CODE,
                VALID_NAME, VALID_DESCRIPTION, CATEGORY_ID, false, true, null,
                null, List.of(), List.of(), now, now);

        assertNotEquals(p1, p2);
    }

    @Test
    void should_BeEqual_When_BothIdsNull() {
        Product p1 = Product.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION,
                CATEGORY_ID, false, null);
        Product p2 = Product.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION,
                CATEGORY_ID, false, null);

        assertEquals(p1, p2);
    }
}
