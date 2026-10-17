package com.cashly.cashly_api.catalog.products.application.ports.out;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.repositories.SpringDataProductRepository;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.adapters.JpaProductRepository;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductReferenceGateway;
import com.cashly.cashly_api.catalog.products.domain.exceptions.ProductReferencedException;
import com.cashly.cashly_api.catalog.products.infrastructure.integration.LocalProductReferenceGatewayAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductReferenceGatewayTest {
    @Test
    void shouldBatchAndDeduplicateAllProviderReferences() {
        var orders = mock(com.cashly.cashly_api.sales.orders.application.ports.in.ProductReferenceQuery.class);
        var invoices = mock(com.cashly.cashly_api.finance.billing.invoices.application.ports.in.ProductReferenceQuery.class);
        var returns = mock(com.cashly.cashly_api.finance.billing.returns.application.ports.in.ProductReferenceQuery.class);
        var warehouses = mock(com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.ProductReferenceQuery.class);
        var first = UUID.randomUUID(); var second = UUID.randomUUID(); var third = UUID.randomUUID();
        var ids = Set.of(first, second, third, UUID.randomUUID());
        when(orders.findReferencedProductIds(ids)).thenReturn(Set.of(first));
        when(invoices.findReferencedProductIds(ids)).thenReturn(Set.of(second));
        when(returns.findReferencedProductIds(ids)).thenReturn(Set.of(first));
        when(warehouses.findReferencedProductIds(ids)).thenReturn(Set.of(third));
        var adapter = new LocalProductReferenceGatewayAdapter(orders, invoices, returns, warehouses);
        assertEquals(Set.of(first, second, third), adapter.findReferencedProductIds(ids));
        verify(orders).findReferencedProductIds(ids);
        verify(invoices).findReferencedProductIds(ids);
        verify(returns).findReferencedProductIds(ids);
        verify(warehouses).findReferencedProductIds(ids);
        assertTrue(adapter.findReferencedProductIds(Set.of()).isEmpty());
        verifyNoMoreInteractions(orders, invoices, returns, warehouses);
    }

    @Test
    void shouldMergeOwnComponentsAndExternalReferencesAndSkipEmptyInput() {
        var repository = mock(SpringDataProductRepository.class);
        var gateway = mock(ProductReferenceGateway.class);
        var ownId = UUID.randomUUID(); var externalId = UUID.randomUUID(); var ids = Set.of(ownId, externalId);
        when(repository.findProductIdsWithComponentReferences(ids)).thenReturn(Set.of(ownId));
        when(gateway.findReferencedProductIds(ids)).thenReturn(Set.of(externalId));
        var adapter = new JpaProductRepository(repository, null, null, null, gateway);
        assertTrue(adapter.findIdsWithReferences(null).isEmpty());
        assertTrue(adapter.findIdsWithReferences(Set.of()).isEmpty());
        verifyNoInteractions(repository, gateway);
        assertEquals(ids, adapter.findIdsWithReferences(ids));
        verify(repository).findProductIdsWithComponentReferences(ids);
        verify(gateway).findReferencedProductIds(ids);
    }

    @Test
    void shouldTranslateIntegrityExceptionAtPersistenceBoundary() {
        var repository = mock(SpringDataProductRepository.class);
        var id = UUID.randomUUID();
        var cause = new DataIntegrityViolationException("FK constraint");
        doThrow(cause).when(repository).deleteById(id);
        var exception = assertThrows(ProductReferencedException.class,
                () -> new JpaProductRepository(repository, null, null, null, null).delete(id));
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldKeepUnexpectedPersistenceFailureUnchanged() {
        var repository = mock(SpringDataProductRepository.class);
        var id = UUID.randomUUID(); var cause = new IllegalStateException("unavailable");
        doThrow(cause).when(repository).deleteById(id);
        assertSame(cause, assertThrows(IllegalStateException.class,
                () -> new JpaProductRepository(repository, null, null, null, null).delete(id)));
    }

    @Test
    void shouldPreserveProviderBatchAndEmptyInputSemantics() {
        var ids = Set.of(UUID.randomUUID());
        var orders = mock(com.cashly.cashly_api.sales.orders.infrastructure.persistence.repositories.SpringDataOrdersProductReferenceRepository.class);
        var invoices = mock(com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.repositories.SpringDataInvoicesProductReferenceRepository.class);
        var returns = mock(com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.repositories.SpringDataReturnsProductReferenceRepository.class);
        var warehouses = mock(com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.repositories.SpringDataWarehousesProductReferenceRepository.class);
        when(orders.findReferencedProductIds(ids)).thenReturn(ids);
        when(invoices.findReferencedProductIds(ids)).thenReturn(ids);
        when(returns.findReferencedProductIds(ids)).thenReturn(ids);
        when(warehouses.findReferencedProductIds(ids)).thenReturn(ids);
        var orderQuery = new com.cashly.cashly_api.sales.orders.infrastructure.persistence.queries.JpaOrdersProductReferenceQuery(orders);
        var invoiceQuery = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.queries.JpaInvoicesProductReferenceQuery(invoices);
        var returnQuery = new com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.queries.JpaReturnsProductReferenceQuery(returns);
        var stockQuery = new com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.queries.JpaWarehousesProductReferenceQuery(warehouses);
        assertTrue(orderQuery.findReferencedProductIds(Set.of()).isEmpty());
        assertTrue(invoiceQuery.findReferencedProductIds(Set.of()).isEmpty());
        assertTrue(returnQuery.findReferencedProductIds(Set.of()).isEmpty());
        assertTrue(stockQuery.findReferencedProductIds(Set.of()).isEmpty());
        assertEquals(ids, orderQuery.findReferencedProductIds(ids));
        assertEquals(ids, invoiceQuery.findReferencedProductIds(ids));
        assertEquals(ids, returnQuery.findReferencedProductIds(ids));
        assertEquals(ids, stockQuery.findReferencedProductIds(ids));
        verify(orders).findReferencedProductIds(ids);
        verify(invoices).findReferencedProductIds(ids);
        verify(returns).findReferencedProductIds(ids);
        verify(warehouses).findReferencedProductIds(ids);
        verifyNoMoreInteractions(orders, invoices, returns, warehouses);
    }
}
