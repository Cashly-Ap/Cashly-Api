package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case.GetStockOverviewUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewGroupBy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses/stock")
public class StockOverviewController {
    private final GetStockOverviewUseCase useCase;

    public StockOverviewController(GetStockOverviewUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES:READ')")
    public StockOverviewResponse getStock(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "NONE") StockOverviewGroupBy groupBy) {
        return useCase.execute(warehouseId, categoryId, search, page, size,
                groupBy);
    }

    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES:READ')")
    public StockOverviewResponse getStock(UUID warehouseId, UUID categoryId,
            String search, int page, int size) {
        return getStock(warehouseId, categoryId, search, page, size,
                StockOverviewGroupBy.NONE);
    }
}
