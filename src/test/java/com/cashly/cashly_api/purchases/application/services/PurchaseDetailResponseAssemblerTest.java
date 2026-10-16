package com.cashly.cashly_api.purchases.application.services;

import com.cashly.cashly_api.purchases.application.ports.out.PurchaseWarehouseGateway;
import com.cashly.cashly_api.purchases.domain.Purchase;
import com.cashly.cashly_api.purchases.domain.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurchaseDetailResponseAssemblerTest {
    private final PurchaseWarehouseGateway warehouses = mock(PurchaseWarehouseGateway.class);
    private final PurchaseDetailResponseAssembler assembler = new PurchaseDetailResponseAssembler(warehouses);

    @Test
    void shouldResolveEachDistinctWarehouseBeforeMappingPurchase() {
        UUID warehouseId = UUID.randomUUID();
        PurchaseLineAllocation first = PurchaseLineAllocation.create(warehouseId, BigDecimal.ONE);
        PurchaseLineAllocation second = PurchaseLineAllocation.create(warehouseId, BigDecimal.TEN);
        PurchaseLine line = PurchaseLine.create(UUID.randomUUID(), "Product", "P-1", BigDecimal.valueOf(11),
                BigDecimal.ONE, List.of(first, second));
        Purchase purchase = Purchase.create(PurchaseNumber.of(2026, 1), UUID.randomUUID(), "Supplier", "J-1",
                UUID.randomUUID(), "Operator", null, List.of(line));
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(
                new PurchaseWarehouseGateway.Warehouse(warehouseId, "WH-1", "Main")));

        var response = assembler.toDetailResponse(purchase);

        verify(warehouses, times(1)).findById(warehouseId);
        assertThat(response.lines().getFirst().allocations()).extracting("warehouseName")
                .containsExactly("Main", "Main");
    }
}
