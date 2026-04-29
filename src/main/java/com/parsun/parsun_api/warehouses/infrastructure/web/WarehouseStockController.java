package com.parsun.parsun_api.warehouses.infrastructure.web;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.application.dto.request.StockAdjustmentRequest;
import com.parsun.parsun_api.warehouses.application.dto.request.StockEntryRequest;
import com.parsun.parsun_api.warehouses.application.dto.request.StockExitRequest;
import com.parsun.parsun_api.warehouses.application.dto.request.TransferStockRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.StockMovementResponse;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseStockResponse;
import com.parsun.parsun_api.warehouses.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses/{warehouseId}/stock")
public class WarehouseStockController {

    private final StockEntryUseCase stockEntryUseCase;
    private final StockExitUseCase stockExitUseCase;
    private final StockAdjustmentUseCase stockAdjustmentUseCase;
    private final TransferStockUseCase transferStockUseCase;
    private final GetWarehouseStockUseCase getWarehouseStockUseCase;

    public WarehouseStockController(StockEntryUseCase stockEntryUseCase,
                                     StockExitUseCase stockExitUseCase,
                                     StockAdjustmentUseCase stockAdjustmentUseCase,
                                     TransferStockUseCase transferStockUseCase,
                                     GetWarehouseStockUseCase getWarehouseStockUseCase) {
        this.stockEntryUseCase = stockEntryUseCase;
        this.stockExitUseCase = stockExitUseCase;
        this.stockAdjustmentUseCase = stockAdjustmentUseCase;
        this.transferStockUseCase = transferStockUseCase;
        this.getWarehouseStockUseCase = getWarehouseStockUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public PageResponse<WarehouseStockResponse> getStock(
            @PathVariable UUID warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return getWarehouseStockUseCase.execute(warehouseId, page, size);
    }

    @PostMapping("/entry")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public List<StockMovementResponse> entry(@PathVariable UUID warehouseId,
                                              @Valid @RequestBody StockEntryRequest request) {
        return stockEntryUseCase.execute(warehouseId, request);
    }

    @PostMapping("/exit")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public List<StockMovementResponse> exit(@PathVariable UUID warehouseId,
                                             @Valid @RequestBody StockExitRequest request) {
        return stockExitUseCase.execute(warehouseId, request);
    }

    @PostMapping("/adjustment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public StockMovementResponse adjustment(@PathVariable UUID warehouseId,
                                             @Valid @RequestBody StockAdjustmentRequest request) {
        return stockAdjustmentUseCase.execute(warehouseId, request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public List<StockMovementResponse> transfer(@PathVariable UUID warehouseId,
                                                 @Valid @RequestBody TransferStockRequest request) {
        return transferStockUseCase.execute(warehouseId, request);
    }
}
