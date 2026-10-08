package com.cashly.cashly_api.products.infrastructure.persistence.repositories;

import com.cashly.cashly_api.products.application.contracts.ProductStockFilterGateway;
import com.cashly.cashly_api.products.infrastructure.integration.LocalProductStockFilterGatewayAdapter;
import com.cashly.cashly_api.warehouses.infrastructure.queries.JpaAvailableStockProductsQuery;
import com.cashly.cashly_api.warehouses.infrastructure.queries.SpringDataAvailableStockProductsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ProductStockFilterTest {
    @Test
    void shouldReturnEmptyStockPageWithoutInvalidEmptyInQuery() {
        var repository = mock(SpringDataProductRepository.class);
        var stocks = mock(ProductStockFilterGateway.class);
        var adapter = new JpaProductRepository(repository, null, null, stocks, null);
        var result = adapter.findAllForOrder("tea", UUID.randomUUID(), false, true, Set.of(), 2, 10);
        assertTrue(result.empty());
        assertEquals(2, result.number());
        assertEquals(10, result.size());
        assertEquals(0, result.totalElements());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldKeepStockFalsePredicateAndPageParameters() {
        var repository = mock(SpringDataProductRepository.class);
        var stocks = mock(ProductStockFilterGateway.class);
        var warehouseIds = Set.of(UUID.randomUUID());
        var productIds = Set.of(UUID.randomUUID());
        var categoryId = UUID.randomUUID();
        when(stocks.availableProductIds(warehouseIds)).thenReturn(productIds);
        when(repository.findAllForOrderWithoutStock(eq("tea"), eq(categoryId), eq(true), eq(productIds), any()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(4, Pageable.class)));
        var result = new JpaProductRepository(repository, null, null, stocks, null)
                .findAllForOrder("tea", categoryId, true, false, warehouseIds, 1, 15);
        assertTrue(result.empty());
        assertEquals(1, result.number());
        assertEquals(15, result.size());
        verify(repository).findAllForOrderWithoutStock(eq("tea"), eq(categoryId), eq(true), eq(productIds), any());
    }

    @Test
    void shouldAvoidStockQueryWhenFilterIsAbsent() {
        var repository = mock(SpringDataProductRepository.class);
        var stocks = mock(ProductStockFilterGateway.class);
        when(repository.findAllForOrder(isNull(), isNull(), isNull(), any()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(3, Pageable.class)));
        var result = new JpaProductRepository(repository, null, null, stocks, null)
                .findAllForOrder(null, null, null, null, Set.of(), 0, 20);
        assertTrue(result.empty());
        verifyNoInteractions(stocks);
    }

    @Test
    void shouldDelegateScopedProductsAndSkipEmptyWarehouseQuery() {
        var repository = mock(SpringDataAvailableStockProductsRepository.class);
        var adapter = new LocalProductStockFilterGatewayAdapter(new JpaAvailableStockProductsQuery(repository));
        assertTrue(adapter.availableProductIds(Set.of()).isEmpty());
        verifyNoInteractions(repository);
        var warehouses = Set.of(UUID.randomUUID());
        var products = Set.of(UUID.randomUUID());
        when(repository.findAvailableProductIds(warehouses)).thenReturn(products);
        assertEquals(products, adapter.availableProductIds(warehouses));
        verify(repository).findAvailableProductIds(warehouses);
    }
}

