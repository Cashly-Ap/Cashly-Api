package com.cashly.cashly_api.inventory.warehouses.stock.domain.entity;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;

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
    private final ReferenceType referenceType;
    private final UUID referenceId;
    private final String referenceNumber;
    private final String notes;
    private final LocalDateTime createdAt;

    public StockMovement(UUID id, UUID warehouseId, UUID productId,
            MovementType movementType, int quantity, int previousStock,
            int newStock, UUID originMovementId, String transferBatchId,
            ReferenceType referenceType, UUID referenceId,
            String referenceNumber, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.originMovementId = originMovementId;
        this.transferBatchId = transferBatchId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static StockMovement create(UUID warehouseId, UUID productId,
            MovementType movementType, int quantity, int previousStock,
            int newStock, UUID originMovementId, String transferBatchId,
            String notes) {
        return new StockMovement(null, warehouseId, productId, movementType,
                quantity, previousStock, newStock, originMovementId,
                transferBatchId, null, null, null, notes, LocalDateTime.now());
    }

    public static StockMovement createWithReference(UUID warehouseId,
            UUID productId, MovementType movementType, int quantity,
            int previousStock, int newStock, UUID originMovementId,
            String transferBatchId, ReferenceType referenceType,
            UUID referenceId, String referenceNumber, String notes) {
        return new StockMovement(null, warehouseId, productId, movementType,
                quantity, previousStock, newStock, originMovementId,
                transferBatchId, referenceType, referenceId, referenceNumber,
                notes, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }
    public UUID getWarehouseId() {
        return warehouseId;
    }
    public UUID getProductId() {
        return productId;
    }
    public MovementType getMovementType() {
        return movementType;
    }
    public int getQuantity() {
        return quantity;
    }
    public int getPreviousStock() {
        return previousStock;
    }
    public int getNewStock() {
        return newStock;
    }
    public UUID getOriginMovementId() {
        return originMovementId;
    }
    public String getTransferBatchId() {
        return transferBatchId;
    }
    public ReferenceType getReferenceType() {
        return referenceType;
    }
    public UUID getReferenceId() {
        return referenceId;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public String getNotes() {
        return notes;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
