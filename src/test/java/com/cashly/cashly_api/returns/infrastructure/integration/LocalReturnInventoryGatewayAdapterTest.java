package com.cashly.cashly_api.returns.infrastructure.integration;

import com.cashly.cashly_api.returns.application.contracts.ReturnInventoryGateway;
import com.cashly.cashly_api.returns.domain.entities.*;
import com.cashly.cashly_api.warehouses.application.api.ReturnInventoryApi;
import com.cashly.cashly_api.warehouses.application.api.ReturnStockCommand;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalReturnInventoryGatewayAdapterTest {
    private final ReturnInventoryApi provider = mock(ReturnInventoryApi.class);
    private final LocalReturnInventoryGatewayAdapter adapter = new LocalReturnInventoryGatewayAdapter(provider);
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final ReturnInventoryGateway.Command command = new ReturnInventoryGateway.Command(
            UUID.randomUUID(), "RET-2026-0001", "User",
            List.of(new ReturnInventoryGateway.Allocation(warehouseId, productId, 2)));

    @Test
    void shouldForwardRestoreMetadataToPublicWarehouseContract() {
        adapter.restoreStock(command);
        verify(provider).restoreStock(expected());
    }

    @Test
    void shouldForwardReverseMetadataToPublicWarehouseContract() {
        adapter.reverseStock(command);
        verify(provider).reverseStock(expected());
    }

    @Test
    void shouldPropagateProviderFailure() {
        IllegalStateException failure = new IllegalStateException("Missing stock");
        doThrow(failure).when(provider).restoreStock(expected());
        assertSame(failure, assertThrows(IllegalStateException.class, () -> adapter.restoreStock(command)));
    }

    @Test
    void shouldPreserveIntegerTruncationAndSkipNonPositiveAllocations() {
        Return returned = mock(Return.class);
        ReturnLine line = mock(ReturnLine.class);
        when(returned.getId()).thenReturn(ReturnId.of(command.returnId()));
        when(returned.getReturnNumber()).thenReturn(ReturnNumber.of(2026, 1));
        when(returned.getLines()).thenReturn(List.of(line));
        when(line.getProductId()).thenReturn(productId);
        ReturnLineAllocation fractional = allocation(new BigDecimal("2.9"));
        ReturnLineAllocation zero = allocation(BigDecimal.ZERO);
        ReturnLineAllocation negative = allocation(BigDecimal.ONE.negate());
        when(line.getAllocations()).thenReturn(List.of(fractional, zero, negative));
        var mapped = ReturnInventoryGateway.Command.from(returned, "User");
        assertEquals(List.of(new ReturnInventoryGateway.Allocation(warehouseId, productId, 2)), mapped.allocations());
        assertEquals(command.returnId(), mapped.returnId());
        assertEquals("User", mapped.userName());
    }

    private ReturnLineAllocation allocation(BigDecimal quantity) {
        ReturnLineAllocation allocation = mock(ReturnLineAllocation.class);
        when(allocation.getAllocated()).thenReturn(quantity);
        when(allocation.getWarehouseId()).thenReturn(warehouseId);
        return allocation;
    }

    private ReturnStockCommand expected() {
        return new ReturnStockCommand(command.returnId(), command.returnNumber(), command.userName(),
                List.of(new ReturnStockCommand.Allocation(warehouseId, productId, 2)));
    }
}
