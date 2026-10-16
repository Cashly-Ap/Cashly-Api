package com.cashly.cashly_api.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.stock.application.adjustment.StockAdjustmentRequest;
import com.cashly.cashly_api.warehouses.stock.application.entry.StockEntryRequest;
import com.cashly.cashly_api.warehouses.stock.application.exit.StockExitRequest;
import com.cashly.cashly_api.warehouses.stock.application.transfer.TransferStockRequest;
import com.cashly.cashly_api.warehouses.stock.application.movements.StockMovementResponse;
import com.cashly.cashly_api.warehouses.stock.application.dto.WarehouseStockResponse;
import com.cashly.cashly_api.warehouses.stock.application.queries.GetWarehouseStockUseCase;
import com.cashly.cashly_api.warehouses.stock.application.adjustment.StockAdjustmentUseCase;
import com.cashly.cashly_api.warehouses.stock.application.entry.StockEntryUseCase;
import com.cashly.cashly_api.warehouses.stock.application.exit.StockExitUseCase;
import com.cashly.cashly_api.warehouses.stock.application.transfer.TransferStockUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PageResponse<WarehouseStockResponse>> getStock(
            @PathVariable UUID warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getWarehouseStockUseCase.execute(warehouseId, page, size));
    }

    @PostMapping("/entry")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<List<StockMovementResponse>> entry(@PathVariable UUID warehouseId,
                                                              @Valid @RequestBody StockEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockEntryUseCase.execute(warehouseId, request));
    }

    @PostMapping("/exit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<List<StockMovementResponse>> exit(@PathVariable UUID warehouseId,
                                                             @Valid @RequestBody StockExitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockExitUseCase.execute(warehouseId, request));
    }

    @PostMapping("/adjustment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<StockMovementResponse> adjustment(@PathVariable UUID warehouseId,
                                                             @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockAdjustmentUseCase.execute(warehouseId, request));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<List<StockMovementResponse>> transfer(@PathVariable UUID warehouseId,
                                                                  @Valid @RequestBody TransferStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferStockUseCase.execute(warehouseId, request));
    }
}
