package com.cashly.cashly_api.products.application.mappers;

import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse.ComponentResponse;
import com.cashly.cashly_api.products.application.dto.response.ProductListResponse;
import com.cashly.cashly_api.products.application.dto.response.ProductPriceDetailResponse;
import com.cashly.cashly_api.products.domain.entity.Product;
import com.cashly.cashly_api.products.domain.entity.ProductComponent;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProductMapper Unit Tests")
class ProductMapperTest {

    private ProductMapper mapper;

    private final UUID productId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final String code = "P001";
    private final String name = "Test Product";
    private final String description = "Test Description";
    private final BigDecimal purchasePrice = new BigDecimal("70.00");
    private final String categoryName = "Electronics";

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    private Product simpleProduct() {
        return Product.reconstitute(
                productId, code, name, description, categoryId,
                false, true, null,
                purchasePrice,
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Product compositeProductWithComponents() {
        UUID componentProductId = UUID.randomUUID();
        ProductComponent component = new ProductComponent(
                UUID.randomUUID(), productId, componentProductId, 2
        );
        Product componentProduct = Product.reconstitute(
                componentProductId, "C001", "Component 1", "Component desc", categoryId,
                false, true, null,
                new BigDecimal("20.00"),
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        return Product.reconstitute(
                productId, code, name, description, categoryId,
                true, true, null,
                purchasePrice,
                List.of(component),
                List.of(ProductPrice.reconstitute(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("120.00"), new BigDecimal("30.00"))),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void should_MapProductToListResponse_When_SimpleProduct() {
        Product product = simpleProduct();

        ProductListResponse response = mapper.toListResponse(product, categoryName, true);

        assertNotNull(response);
        assertEquals(productId.toString(), response.id());
        assertEquals(code, response.code());
        assertEquals(name, response.name());
        assertEquals(categoryId.toString(), response.categoryId());
        assertEquals(categoryName, response.categoryName());
        assertEquals(false, response.composite());
        assertEquals(true, response.active());
        assertEquals(purchasePrice, response.purchasePrice());
        assertTrue(response.productPrices().isEmpty());
        assertTrue(response.deletable());
    }

    @Test
    void should_MapProductToListResponse_When_ProductHasPrices() {
        UUID priceTypeId = UUID.randomUUID();
        Product product = Product.reconstitute(
                productId, code, name, description, categoryId,
                false, true, null,
                purchasePrice,
                List.of(),
                List.of(ProductPrice.reconstitute(UUID.randomUUID(), priceTypeId, new BigDecimal("120.00"), null)),
                LocalDateTime.now(), LocalDateTime.now()
        );

        ProductListResponse response = mapper.toListResponse(product, categoryName, true);

        assertEquals(1, response.productPrices().size());
        assertEquals(priceTypeId.toString(), response.productPrices().get(0).priceTypeId());
        assertEquals(new BigDecimal("120.00"), response.productPrices().get(0).price());
    }

    @Test
    void should_MapProductToDetailResponse_When_CompositeProductWithComponents() {
        Product product = compositeProductWithComponents();

        ProductDetailResponse response = mapper.toDetailResponse(product, categoryName, Map.of(), Map.of());

        assertNotNull(response);
        assertEquals(productId.toString(), response.id());
        assertEquals(true, response.composite());
        assertEquals(1, response.components().size());
        assertEquals(1, response.productPrices().size());

        ComponentResponse component = response.components().get(0);
        assertEquals("", component.componentProductCode());
        assertEquals("", component.componentProductName());
        assertEquals(2, component.quantity());
    }

    @Test
    void should_MapProductToDetailResponse_When_ComponentLookupProvided() {
        Product product = compositeProductWithComponents();
        UUID componentProductId = product.getComponents().get(0).getComponentProductId();
        Product componentProduct = Product.reconstitute(
                componentProductId, "C001", "Component 1", "Component desc", categoryId,
                false, true, null,
                new BigDecimal("20.00"),
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        ProductDetailResponse response = mapper.toDetailResponse(
                product, categoryName,
                Map.of(componentProductId, componentProduct),
                Map.of()
        );

        ComponentResponse component = response.components().get(0);
        assertEquals("C001", component.componentProductCode());
        assertEquals("Component 1", component.componentProductName());
    }

    @Test
    void should_MapProductToDetailResponse_When_PriceTypeNamesProvided() {
        UUID priceTypeId = UUID.randomUUID();
        Product product = Product.reconstitute(
                productId, code, name, description, categoryId,
                false, true, null,
                purchasePrice,
                List.of(),
                List.of(ProductPrice.reconstitute(UUID.randomUUID(), priceTypeId, new BigDecimal("120.00"), null)),
                LocalDateTime.now(), LocalDateTime.now()
        );

        ProductDetailResponse response = mapper.toDetailResponse(
                product, categoryName, Map.of(),
                Map.of(priceTypeId, "Majorista")
        );

        ProductPriceDetailResponse priceResponse = response.productPrices().get(0);
        assertEquals("Majorista", priceResponse.priceTypeName());
    }

    @Test
    void should_MapProductToDetailResponse_When_AllFieldsPresent() {
        Product product = simpleProduct();

        ProductDetailResponse response = mapper.toDetailResponse(product, categoryName, Map.of(), Map.of());

        assertEquals(productId.toString(), response.id());
        assertEquals(code, response.code());
        assertEquals(name, response.name());
        assertEquals(description, response.description());
        assertEquals(categoryId.toString(), response.categoryId());
        assertEquals(categoryName, response.categoryName());
        assertEquals(false, response.composite());
        assertEquals(true, response.active());
        assertNull(response.photo());
        assertEquals(purchasePrice, response.purchasePrice());
        assertTrue(response.components().isEmpty());
        assertTrue(response.productPrices().isEmpty());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
    }

    private Product productWithPrices() {
        UUID priceTypeId = UUID.randomUUID();
        return Product.reconstitute(
                productId, code, name, description, categoryId,
                false, true, null,
                purchasePrice,
                List.of(),
                List.of(ProductPrice.reconstitute(UUID.randomUUID(), priceTypeId, new BigDecimal("120.00"), null)),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
