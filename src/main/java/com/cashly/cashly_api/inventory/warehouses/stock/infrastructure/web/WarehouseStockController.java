package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.adjustment.request.StockAdjustmentRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.entry.request.StockEntryRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exit.request.StockExitRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.request.TransferStockRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.WarehouseStockResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case.GetWarehouseStockUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.adjustment.use_case.StockAdjustmentUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.entry.use_case.StockEntryUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exit.use_case.StockExitUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.use_case.TransferStockUseCase;
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
    @PreAuthorize("hasAuthority('INVENTORY:READ')")
    public ResponseEntity<PageResponse<WarehouseStockResponse>> getStock(
            @PathVariable UUID warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getWarehouseStockUseCase.execute(warehouseId, page, size));
    }

    @PostMapping("/entry")
    @PreAuthorize("hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<List<StockMovementResponse>> entry(@PathVariable UUID warehouseId,
                                                              @Valid @RequestBody StockEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockEntryUseCase.execute(warehouseId, request));
    }

    @PostMapping("/exit")
    @PreAuthorize("hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<List<StockMovementResponse>> exit(@PathVariable UUID warehouseId,
                                                             @Valid @RequestBody StockExitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockExitUseCase.execute(warehouseId, request));
    }

    @PostMapping("/adjustment")
    @PreAuthorize("hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<StockMovementResponse> adjustment(@PathVariable UUID warehouseId,
                                                             @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockAdjustmentUseCase.execute(warehouseId, request));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<List<StockMovementResponse>> transfer(@PathVariable UUID warehouseId,
                                                                  @Valid @RequestBody TransferStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferStockUseCase.execute(warehouseId, request));
    }
}
