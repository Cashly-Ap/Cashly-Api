package com.cashly.cashly_api.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.stock.application.movements.StockMovementResponse;
import com.cashly.cashly_api.warehouses.stock.application.movements.GetStockMovementsUseCase;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public PageResponse<StockMovementResponse> getMovements(
            @PathVariable UUID warehouseId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return getStockMovementsUseCase.execute(warehouseId, productId, movementType, page, size);
    }
}
