package com.cashly.cashly_api.catalog.products.application.transactions.use_case;

import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import com.cashly.cashly_api.catalog.products.infrastructure.integration.LocalProductCategoryGatewayAdapter;
import com.cashly.cashly_api.catalog.categories.application.queries.services.RepositoryProductCategoryQuery;
import com.cashly.cashly_api.catalog.products.application.ports.out.CustomerProductContextGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductWarehouseStockGateway;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.domain.valueobjects.ProductPrice;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductsForTransactionUseCaseTest {
    private final CustomerProductContextGateway customers = mock(CustomerProductContextGateway.class);
    private final ProductWarehouseStockGateway stocks = mock(ProductWarehouseStockGateway.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final GetProductsForTransactionUseCase useCase = new GetProductsForTransactionUseCase(customers, products, new LocalProductCategoryGatewayAdapter(new RepositoryProductCategoryQuery(categories)), stocks);
    private final UUID customerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID priceTypeId = UUID.randomUUID();

    @Test
    void shouldPreserveFirstCustomerPricingUnionPermissionsAndPagination() {
        UUID secondCustomerId = UUID.randomUUID();
        UUID secondWarehouseId = UUID.randomUUID();
        UUID missingWarehouseId = UUID.randomUUID();
        List<UUID> customerIds = List.of(customerId, secondCustomerId);
        when(customers.requirePriceType(customerIds)).thenReturn(priceTypeId);
        when(customers.accessibleWarehouseIds(customerId)).thenReturn(Set.of(warehouseId, missingWarehouseId));
        when(customers.accessibleWarehouseIds(secondCustomerId)).thenReturn(Set.of(secondWarehouseId));
        Set<UUID> allowed = Set.of(warehouseId, secondWarehouseId, missingWarehouseId);
        Product product = product();
        when(product.getProductPrices()).thenReturn(List.of(ProductPrice.create(priceTypeId, BigDecimal.TEN, null)));
        when(products.findAllForOrder("Search", categoryId, false, true, allowed, 1, 2))
                .thenReturn(new PageResponse<>(List.of(product), 5, 3, 2, 1, false, false, false));
        when(stocks.findByProductId(productId)).thenReturn(List.of(
                stock(warehouseId, true, 10), stock(secondWarehouseId, true, 20),
                stock(missingWarehouseId, false, 30), stock(UUID.randomUUID(), true, 40)));
        var response = useCase.execute(customerIds, "Search", categoryId, false, true, 1, 2);
        var item = response.content().getFirst();
        assertEquals(BigDecimal.TEN, item.customerPrice());
        assertEquals(30, item.totalStock());
        assertEquals(2, item.stock().size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals(1, response.number());
        assertEquals(2, response.size());
        verify(customers).requirePriceType(customerIds);
        verify(customers).accessibleWarehouseIds(customerId);
        verify(customers).accessibleWarehouseIds(secondCustomerId);
    }

    @Test
    void shouldUsePurchasePriceWhenFirstCustomerHasNoPriceType() {
        assertPurchasePriceFallback(null);
    }

    @Test
    void shouldUsePurchasePriceWhenCustomerPriceTypeHasNoMatchingPrice() {
        assertPurchasePriceFallback(priceTypeId);
    }

    private void assertPurchasePriceFallback(UUID pricing) {
        when(customers.requirePriceType(List.of(customerId))).thenReturn(pricing);
        when(customers.accessibleWarehouseIds(customerId)).thenReturn(Set.of(warehouseId));
        Product product = product();
        when(products.findAllForOrder(null, null, null, null, Set.of(warehouseId), 0, 10))
                .thenReturn(new PageResponse<>(List.of(product), 1, 1, 10, 0, true, true, false));
        var response = useCase.execute(customerId, null, null, null, null, 0, 10);
        assertEquals(new BigDecimal("5"), response.content().getFirst().customerPrice());
        assertEquals(0, response.content().getFirst().totalStock());
        assertTrue(response.content().getFirst().stock().isEmpty());
    }

    private Product product() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(productId);
        when(product.getCategoryId()).thenReturn(categoryId);
        when(product.getPurchasePrice()).thenReturn(new BigDecimal("5"));
        return product;
    }

    private ProductWarehouseStockGateway.Stock stock(UUID id, boolean exists, int quantity) {
        return new ProductWarehouseStockGateway.Stock(id, "WH", "Warehouse", exists, quantity, 2,
                quantity - 2, LocalDateTime.now());
    }
}
