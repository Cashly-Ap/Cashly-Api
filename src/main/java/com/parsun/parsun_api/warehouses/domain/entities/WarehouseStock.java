package com.parsun.parsun_api.warehouses.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class WarehouseStock {

    private final UUID id;
    private final UUID warehouseId;
    private final UUID productId;
    private int quantity;
    private LocalDateTime lastMovementAt;
    private LocalDateTime updatedAt;

    public WarehouseStock(UUID id, UUID warehouseId, UUID productId, int quantity,
                          LocalDateTime lastMovementAt, LocalDateTime updatedAt) {
        Objects.requireNonNull(warehouseId, "Warehouse ID cannot be null");
        Objects.requireNonNull(productId, "Product ID cannot be null");
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.quantity = quantity;
        this.lastMovementAt = lastMovementAt;
        this.updatedAt = updatedAt;
    }

    public static WarehouseStock create(UUID warehouseId, UUID productId) {
        LocalDateTime now = LocalDateTime.now();
        return new WarehouseStock(null, warehouseId, productId, 0, now, now);
    }

    public void addStock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount to add must be positive");
        this.quantity += amount;
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount to remove must be positive");
        if (this.quantity < amount) {
            throw new IllegalStateException("Insufficient stock: available=" + this.quantity + ", requested=" + amount);
        }
        this.quantity -= amount;
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void adjustStock(int newQuantity) {
        if (newQuantity < 0) throw new IllegalArgumentException("Adjusted quantity cannot be negative");
        this.quantity = newQuantity;
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getWarehouseId() { return warehouseId; }
    public UUID getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getLastMovementAt() { return lastMovementAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
