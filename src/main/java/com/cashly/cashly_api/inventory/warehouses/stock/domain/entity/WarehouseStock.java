package com.cashly.cashly_api.inventory.warehouses.stock.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class WarehouseStock {

    private final UUID id;
    private final UUID warehouseId;
    private final UUID productId;
    private int quantity;
    private int reserved;
    private LocalDateTime lastMovementAt;
    private LocalDateTime updatedAt;

    public WarehouseStock(UUID id, UUID warehouseId, UUID productId,
            int quantity, int reserved, LocalDateTime lastMovementAt,
            LocalDateTime updatedAt) {
        Objects.requireNonNull(warehouseId, "Warehouse ID cannot be null");
        Objects.requireNonNull(productId, "Product ID cannot be null");
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.quantity = quantity;
        this.reserved = reserved;
        this.lastMovementAt = lastMovementAt;
        this.updatedAt = updatedAt;
    }

    public static WarehouseStock create(UUID warehouseId, UUID productId) {
        LocalDateTime now = LocalDateTime.now();
        return new WarehouseStock(null, warehouseId, productId, 0, 0, now, now);
    }

    public void addStock(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException(
                    "Amount to add must be positive");
        this.quantity += amount;
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException(
                    "Amount to remove must be positive");
        if (this.quantity < amount) {
            throw new IllegalStateException("Insufficient stock: available="
                    + this.quantity + ", requested=" + amount);
        }
        this.quantity -= amount;
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void adjustStock(int newQuantity) {
        if (newQuantity < 0)
            throw new IllegalArgumentException(
                    "Adjusted quantity cannot be negative");
        this.quantity = newQuantity;
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reserve(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException(
                    "Amount to reserve must be positive");
        if (this.quantity - this.reserved < amount) {
            throw new IllegalStateException(
                    "Insufficient available stock: available="
                            + (this.quantity - this.reserved) + ", requested="
                            + amount);
        }
        this.reserved += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void releaseReserved(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException(
                    "Amount to release must be positive");
        if (this.reserved < amount) {
            throw new IllegalStateException(
                    "Cannot release more than reserved: reserved="
                            + this.reserved + ", requested=" + amount);
        }
        this.reserved -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void deductReserved(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException(
                    "Amount to deduct must be positive");
        if (this.reserved < amount) {
            throw new IllegalStateException(
                    "Cannot deduct more than reserved: reserved="
                            + this.reserved + ", requested=" + amount);
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("Insufficient stock: available="
                    + this.quantity + ", requested=" + amount);
        }
        this.quantity -= amount;
        this.reserved -= amount;
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getAvailableQuantity() {
        return this.quantity - this.reserved;
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
    public int getQuantity() {
        return quantity;
    }
    public int getReserved() {
        return reserved;
    }
    public LocalDateTime getLastMovementAt() {
        return lastMovementAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
