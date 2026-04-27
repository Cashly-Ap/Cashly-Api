package com.parsun.parsun_api.warehouses.infrastructure.web;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.application.dto.response.StockMovementResponse;
import com.parsun.parsun_api.warehouses.application.usecases.GetStockMovementsUseCase;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return getStockMovementsUseCase.execute(warehouseId, productId, page, size);
    }
}
