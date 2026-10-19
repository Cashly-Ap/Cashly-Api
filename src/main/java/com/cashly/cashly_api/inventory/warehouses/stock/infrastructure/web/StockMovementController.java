package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case.GetStockMovementsUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses/{warehouseId}/movements")
public class StockMovementController {

    private final GetStockMovementsUseCase getStockMovementsUseCase;

    public StockMovementController(GetStockMovementsUseCase getStockMovementsUseCase) {
        this.getStockMovementsUseCase = getStockMovementsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY:READ')")
    public PageResponse<StockMovementResponse> getMovements(
            @PathVariable UUID warehouseId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return getStockMovementsUseCase.execute(warehouseId, productId, movementType, page, size);
    }
}
