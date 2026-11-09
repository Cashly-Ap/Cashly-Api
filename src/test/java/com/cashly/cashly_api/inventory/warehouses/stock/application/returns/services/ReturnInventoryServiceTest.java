package com.cashly.cashly_api.inventory.warehouses.stock.application.returns.services;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.ReturnStockCommand;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReturnInventoryServiceTest {
    private final WarehouseStockRepository stocks = mock(
            WarehouseStockRepository.class);
    private final StockMovementRepository movements = mock(
            StockMovementRepository.class);
    private final com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.ReturnInventoryApi inventory = new com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.config.ReturnInventoryConfig()
            .returnInventoryApi(stocks, movements);
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final ReturnStockCommand command = new ReturnStockCommand(
            UUID.randomUUID(), "RET-2026-0001", "User",
            List.of(new ReturnStockCommand.Allocation(warehouseId, productId,
                    2)));

    @Test
    void shouldRestoreLockedStockAndPreserveReturnEntryMetadata() {
        WarehouseStock stock = stockWithQuantity(10);
        inventory.restoreStock(command);
        assertEquals(12, stock.getQuantity());
        verifyMovement(MovementType.RETURN_ENTRY, 10, 12,
                "Return: " + command.returnNumber());
        verify(stocks).save(stock);
        verify(stocks, never()).findByWarehouseIdAndProductId(any(), any());
    }

    @Test
    void shouldReverseLockedStockAndPreserveDeletionMetadata() {
        WarehouseStock stock = stockWithQuantity(10);
        inventory.reverseStock(command);
        assertEquals(8, stock.getQuantity());
        verifyMovement(MovementType.CANCEL_ENTRY, 10, 8,
                "Return deleted: " + command.returnNumber() + " by User");
        verify(stocks).save(stock);
    }

    @Test
    void shouldCreateStockWhenReturnIsDirectedToWarehouseWithoutStock() {
        WarehouseStock created = WarehouseStock.create(warehouseId, productId);
        when(stocks.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId,
                productId)).thenReturn(created);

        inventory.restoreStock(command);

        assertEquals(2, created.getQuantity());
        verify(stocks).save(created);
        verifyMovement(MovementType.RETURN_ENTRY, 0, 2,
                "Return: " + command.returnNumber());
    }

    @Test
    void shouldFailReverseWhenStockIsMissing() {
        assertThrows(IllegalStateException.class,
                () -> inventory.reverseStock(command));
        verify(stocks, never()).save(any());
        verifyNoInteractions(movements);
    }

    @Test
    void shouldRejectInsufficientStockWithoutSavingOrRecordingMovement() {
        WarehouseStock stock = stockWithQuantity(1);
        assertThrows(IllegalStateException.class,
                () -> inventory.reverseStock(command));
        assertEquals(1, stock.getQuantity());
        verify(stocks, never()).save(any());
        verifyNoInteractions(movements);
    }

    @Test
    void shouldIgnoreCommandsWithoutPositiveAllocations() {
        var empty = new ReturnStockCommand(command.returnId(),
                command.returnNumber(), null, List.of());
        inventory.restoreStock(empty);
        inventory.reverseStock(empty);
        verifyNoInteractions(stocks, movements);
    }

    @Test
    void shouldRejectInvalidPublicStockCommands() {
        assertThrows(NullPointerException.class,
                () -> new ReturnStockCommand(null, "RET", null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new ReturnStockCommand(UUID.randomUUID(), null, null,
                        List.of()));
        assertThrows(NullPointerException.class,
                () -> new ReturnStockCommand.Allocation(null, productId, 1));
        assertThrows(NullPointerException.class,
                () -> new ReturnStockCommand.Allocation(warehouseId, null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ReturnStockCommand.Allocation(warehouseId, productId,
                        0));
        assertThrows(IllegalArgumentException.class,
                () -> new ReturnStockCommand.Allocation(warehouseId, productId,
                        -1));
        assertThrows(UnsupportedOperationException.class,
                () -> command.allocations().clear());
    }
    private WarehouseStock stockWithQuantity(int quantity) {
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(quantity);
        when(stocks.findByWarehouseIdAndProductIdForUpdate(warehouseId,
                productId)).thenReturn(Optional.of(stock));
        when(stocks.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId,
                productId)).thenReturn(stock);
        return stock;
    }

    private void verifyMovement(MovementType type, int previous, int current,
            String notes) {
        var captor = org.mockito.ArgumentCaptor.forClass(StockMovement.class);
        verify(movements).save(captor.capture());
        StockMovement movement = captor.getValue();
        assertEquals(type, movement.getMovementType());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(2, movement.getQuantity());
        assertEquals(previous, movement.getPreviousStock());
        assertEquals(current, movement.getNewStock());
        assertEquals(ReferenceType.RETURN, movement.getReferenceType());
        assertEquals(command.returnId(), movement.getReferenceId());
        assertEquals(command.returnNumber(), movement.getReferenceNumber());
        assertEquals(notes, movement.getNotes());
        assertNull(movement.getOriginMovementId());
        assertNull(movement.getTransferBatchId());
    }
}
