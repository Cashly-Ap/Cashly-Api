package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case.GetStockMovementsUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses")
public class StockMovementController {

    private final GetStockMovementsUseCase getStockMovementsUseCase;

    public StockMovementController(
            GetStockMovementsUseCase getStockMovementsUseCase) {
        this.getStockMovementsUseCase = getStockMovementsUseCase;
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES_MOVEMENT_HISTORY')")
    public StockMovementsResponse getGlobalMovements(
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String groupBy) {
        return getStockMovementsUseCase.execute(new StockMovementsQuery(
                warehouseId, productId, movementType, search, page, size,
                groupBy).toFilter());
    }

    @GetMapping("/{warehouseId}/movements")
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES_MOVEMENT_HISTORY')")
    public PageResponse<StockMovementResponse> getMovements(
            @PathVariable UUID warehouseId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return getStockMovementsUseCase.execute(warehouseId, productId,
                movementType, page, size);
    }
}
