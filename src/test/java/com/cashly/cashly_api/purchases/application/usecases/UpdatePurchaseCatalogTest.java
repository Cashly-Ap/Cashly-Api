package com.cashly.cashly_api.purchases.application.usecases;

import com.cashly.cashly_api.purchases.application.contracts.PurchaseProductGateway;
import com.cashly.cashly_api.purchases.application.dto.request.UpdatePurchaseRequest;
import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.purchases.application.contracts.PurchaseWarehouseGateway;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class UpdatePurchaseCatalogTest {
    @Test
    void shouldSnapshotProductIdentityAndPreserveRequestedCostAndQuantity() {
        var repository = mock(PurchaseRepository.class);
        var products = mock(PurchaseProductGateway.class);
        var id = UUID.randomUUID(); var productId = UUID.randomUUID();
        var purchase = Purchase.create(PurchaseNumber.of(2026, 1), UUID.randomUUID(), "Supplier", "J-123",
                UUID.randomUUID(), "Operator", "old", List.of(PurchaseLine.create(productId, "Old", "Old", BigDecimal.ONE, BigDecimal.TEN, List.of())));
        when(repository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(products.findById(productId)).thenReturn(Optional.of(new PurchaseProductGateway.Entry(productId, "PRD-004", "Tea")));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new UpdatePurchaseRequest("updated", List.of(new UpdatePurchaseRequest.LineRequest(
                productId, new BigDecimal("2.50"), new BigDecimal("7.20"), List.of())));
        var result = new UpdatePurchaseUseCase(repository, products, new com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler(mock(PurchaseWarehouseGateway.class))).execute(id, request);
        assertEquals("updated", result.notes());
        assertEquals("Tea", result.lines().getFirst().productName());
        assertEquals("PRD-004", result.lines().getFirst().productCode());
        assertEquals(new BigDecimal("2.50"), result.lines().getFirst().quantity());
        assertEquals(new BigDecimal("7.20"), result.lines().getFirst().unitCost());
        assertEquals(new BigDecimal("18.0000"), result.subtotal());
    }

    @Test
    void shouldRejectMissingProductBeforeMutatingPurchase() {
        var repository = mock(PurchaseRepository.class);
        var products = mock(PurchaseProductGateway.class);
        var id = UUID.randomUUID(); var productId = UUID.randomUUID();
        var purchase = Purchase.create(PurchaseNumber.of(2026, 1), UUID.randomUUID(), "Supplier", "J-123",
                UUID.randomUUID(), "Operator", "old", List.of(PurchaseLine.create(productId, "Old", "Old", BigDecimal.ONE, BigDecimal.TEN, List.of())));
        when(repository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        var request = new UpdatePurchaseRequest("changed", List.of(new UpdatePurchaseRequest.LineRequest(productId, BigDecimal.TEN, BigDecimal.ONE, List.of())));
        var exception = assertThrows(IllegalArgumentException.class,
                () -> new UpdatePurchaseUseCase(repository, products, new com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler(mock(PurchaseWarehouseGateway.class))).execute(id, request));
        assertEquals("Product not found: " + productId, exception.getMessage());
        assertEquals("old", purchase.getNotes());
        assertEquals("Old", purchase.getLines().getFirst().getProductName());
        verify(repository, never()).save(any());
    }
}

