package com.parsun.parsun_api.warehouses.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockMovement {

    private final UUID id;
    private final UUID warehouseId;
    private final UUID productId;
    private final MovementType movementType;
    private final int quantity;
    private final int previousStock;
    private final int newStock;
    private final UUID originMovementId;
    private final String transferBatchId;
    private final String notes;
    private final LocalDateTime createdAt;

    public StockMovement(UUID id, UUID warehouseId, UUID productId, MovementType movementType,
                         int quantity, int previousStock, int newStock,
                         UUID originMovementId, String transferBatchId, String notes,
                         LocalDateTime createdAt) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.originMovementId = originMovementId;
        this.transferBatchId = transferBatchId;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static StockMovement create(UUID warehouseId, UUID productId, MovementType movementType,
                                        int quantity, int previousStock, int newStock,
                                        UUID originMovementId, String transferBatchId, String notes) {
        return new StockMovement(null, warehouseId, productId, movementType, quantity,
                previousStock, newStock, originMovementId, transferBatchId, notes, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public UUID getWarehouseId() { return warehouseId; }
    public UUID getProductId() { return productId; }
    public MovementType getMovementType() { return movementType; }
    public int getQuantity() { return quantity; }
    public int getPreviousStock() { return previousStock; }
    public int getNewStock() { return newStock; }
    public UUID getOriginMovementId() { return originMovementId; }
    public String getTransferBatchId() { return transferBatchId; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
