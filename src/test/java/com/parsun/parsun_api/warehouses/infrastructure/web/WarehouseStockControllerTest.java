package com.parsun.parsun_api.warehouses.infrastructure.web;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.application.dto.request.StockAdjustmentRequest;
import com.parsun.parsun_api.warehouses.application.dto.request.StockEntryRequest;
import com.parsun.parsun_api.warehouses.application.dto.request.StockExitRequest;
import com.parsun.parsun_api.warehouses.application.dto.request.TransferStockRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.StockMovementResponse;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseStockResponse;
import com.parsun.parsun_api.warehouses.application.usecases.GetWarehouseStockUseCase;
import com.parsun.parsun_api.warehouses.application.usecases.StockAdjustmentUseCase;
import com.parsun.parsun_api.warehouses.application.usecases.StockEntryUseCase;
import com.parsun.parsun_api.warehouses.application.usecases.StockExitUseCase;
import com.parsun.parsun_api.warehouses.application.usecases.TransferStockUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseStockController Unit Tests")
class WarehouseStockControllerTest {

    @Mock
    private StockEntryUseCase stockEntryUseCase;

    @Mock
    private StockExitUseCase stockExitUseCase;

    @Mock
    private StockAdjustmentUseCase stockAdjustmentUseCase;

    @Mock
    private TransferStockUseCase transferStockUseCase;

    @Mock
    private GetWarehouseStockUseCase getWarehouseStockUseCase;

    @InjectMocks
    private WarehouseStockController warehouseStockController;

    @Test
    @DisplayName("should get stock and return page")
    void should_get_stock_and_return_page() {
        UUID warehouseId = UUID.randomUUID();
        WarehouseStockResponse stock = mock(WarehouseStockResponse.class);
        PageResponse<WarehouseStockResponse> pageResponse = new PageResponse<>(
                List.of(stock), 1L, 1, 20, 0, true, true, false);

        when(getWarehouseStockUseCase.execute(eq(warehouseId), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        PageResponse<WarehouseStockResponse> result = warehouseStockController.getStock(warehouseId, 0, 20);

        assertEquals(1, result.content().size());
        verify(getWarehouseStockUseCase).execute(warehouseId, 0, 20);
    }

    @Test
    @DisplayName("should return empty stock page")
    void should_return_empty_stock_page() {
        UUID warehouseId = UUID.randomUUID();
        PageResponse<WarehouseStockResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);

        when(getWarehouseStockUseCase.execute(eq(warehouseId), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        PageResponse<WarehouseStockResponse> result = warehouseStockController.getStock(warehouseId, 0, 20);

        assertTrue(result.content().isEmpty());
        verify(getWarehouseStockUseCase).execute(warehouseId, 0, 20);
    }

    @Test
    @DisplayName("should create stock entry and return movements")
    void should_create_stock_entry_and_return_movements() {
        UUID warehouseId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);

        when(stockEntryUseCase.execute(eq(warehouseId), any(StockEntryRequest.class)))
                .thenReturn(List.of(movement));

        var request = new StockEntryRequest(List.of());

        List<StockMovementResponse> result = warehouseStockController.entry(warehouseId, request);

        assertEquals(1, result.size());
        verify(stockEntryUseCase).execute(eq(warehouseId), any(StockEntryRequest.class));
    }

    @Test
    @DisplayName("should create stock exit and return movements")
    void should_create_stock_exit_and_return_movements() {
        UUID warehouseId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);

        when(stockExitUseCase.execute(eq(warehouseId), any(StockExitRequest.class)))
                .thenReturn(List.of(movement));

        var request = new StockExitRequest(List.of());

        List<StockMovementResponse> result = warehouseStockController.exit(warehouseId, request);

        assertEquals(1, result.size());
        verify(stockExitUseCase).execute(eq(warehouseId), any(StockExitRequest.class));
    }

    @Test
    @DisplayName("should create stock adjustment and return movement")
    void should_create_stock_adjustment_and_return_movement() {
        UUID warehouseId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);

        when(stockAdjustmentUseCase.execute(eq(warehouseId), any(StockAdjustmentRequest.class)))
                .thenReturn(movement);

        StockAdjustmentRequest request = new StockAdjustmentRequest(UUID.randomUUID(), 50, "Adjustment");

        StockMovementResponse result = warehouseStockController.adjustment(warehouseId, request);

        assertNotNull(result);
        verify(stockAdjustmentUseCase).execute(eq(warehouseId), any(StockAdjustmentRequest.class));
    }

    @Test
    @DisplayName("should transfer stock and return movements")
    void should_transfer_stock_and_return_movements() {
        UUID warehouseId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);

        when(transferStockUseCase.execute(eq(warehouseId), any(TransferStockRequest.class)))
                .thenReturn(List.of(movement));

        TransferStockRequest request = new TransferStockRequest(
                UUID.randomUUID(), UUID.randomUUID(), 10, "Transfer notes");

        List<StockMovementResponse> result = warehouseStockController.transfer(warehouseId, request);

        assertEquals(1, result.size());
        verify(transferStockUseCase).execute(eq(warehouseId), any(TransferStockRequest.class));
    }
}
