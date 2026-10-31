package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case.GetStockMovementsUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
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
@DisplayName("StockMovementController Unit Tests")
class StockMovementControllerTest {

    @Mock
    private GetStockMovementsUseCase getStockMovementsUseCase;

    @InjectMocks
    private StockMovementController stockMovementController;

    @Test
    @DisplayName("should get movements and return page")
    void should_get_movements_and_return_page() {
        UUID warehouseId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);
        PageResponse<StockMovementResponse> pageResponse = new PageResponse<>(
                List.of(movement), 1L, 1, 20, 0, true, true, false);

        when(getStockMovementsUseCase.execute(eq(warehouseId), any(), any(),
                anyInt(), anyInt())).thenReturn(pageResponse);

        PageResponse<StockMovementResponse> result = stockMovementController
                .getMovements(warehouseId, null, null, 0, 20);

        assertEquals(1, result.content().size());
        verify(getStockMovementsUseCase).execute(warehouseId, null, null, 0,
                20);
    }

    @Test
    @DisplayName("should get movements filtered by product")
    void should_get_movements_filtered_by_product() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);
        PageResponse<StockMovementResponse> pageResponse = new PageResponse<>(
                List.of(movement), 1L, 1, 20, 0, true, true, false);

        when(getStockMovementsUseCase.execute(eq(warehouseId), eq(productId),
                any(), anyInt(), anyInt())).thenReturn(pageResponse);

        PageResponse<StockMovementResponse> result = stockMovementController
                .getMovements(warehouseId, productId, null, 0, 20);

        assertEquals(1, result.content().size());
        verify(getStockMovementsUseCase).execute(warehouseId, productId, null,
                0, 20);
    }

    @Test
    @DisplayName("should get movements filtered by movement type")
    void should_get_movements_filtered_by_movement_type() {
        UUID warehouseId = UUID.randomUUID();
        PageResponse<StockMovementResponse> pageResponse = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);

        when(getStockMovementsUseCase.execute(warehouseId, null,
                MovementType.EXIT, 0, 20)).thenReturn(pageResponse);

        stockMovementController.getMovements(warehouseId, null,
                MovementType.EXIT, 0, 20);

        verify(getStockMovementsUseCase).execute(warehouseId, null,
                MovementType.EXIT, 0, 20);
    }

    @Test
    @DisplayName("should return empty movements page")
    void should_return_empty_movements_page() {
        UUID warehouseId = UUID.randomUUID();
        PageResponse<StockMovementResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);

        when(getStockMovementsUseCase.execute(eq(warehouseId), any(), any(),
                anyInt(), anyInt())).thenReturn(emptyPage);

        PageResponse<StockMovementResponse> result = stockMovementController
                .getMovements(warehouseId, null, null, 0, 20);

        assertTrue(result.content().isEmpty());
        verify(getStockMovementsUseCase).execute(warehouseId, null, null, 0,
                20);
    }
}
