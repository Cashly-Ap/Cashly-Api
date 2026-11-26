package com.cashly.cashly_api.inventory.warehouses.stock.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class WarehouseStock {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final UUID id;
    private final UUID warehouseId;
    private final UUID productId;
    private BigDecimal quantity;
    private BigDecimal reserved;
    private LocalDateTime lastMovementAt;
    private LocalDateTime updatedAt;

    public WarehouseStock(UUID id, UUID warehouseId, UUID productId,
            BigDecimal quantity, BigDecimal reserved,
            LocalDateTime lastMovementAt, LocalDateTime updatedAt) {
        Objects.requireNonNull(warehouseId, "Warehouse ID cannot be null");
        Objects.requireNonNull(productId, "Product ID cannot be null");
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.quantity = quantity == null ? ZERO : quantity;
        this.reserved = reserved == null ? ZERO : reserved;
        this.lastMovementAt = lastMovementAt;
        this.updatedAt = updatedAt;
    }

    public static WarehouseStock create(UUID warehouseId, UUID productId) {
        LocalDateTime now = LocalDateTime.now();
        return new WarehouseStock(null, warehouseId, productId, ZERO, ZERO,
                now, now);
    }

    public void addStock(BigDecimal amount) {
        requirePositive(amount, "Amount to add");
        this.quantity = this.quantity.add(amount);
        touch();
    }

    public void removeStock(BigDecimal amount) {
        requirePositive(amount, "Amount to remove");
        if (this.quantity.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient stock: available="
                    + this.quantity.toPlainString() + ", requested="
                    + amount.toPlainString());
        }
        this.quantity = this.quantity.subtract(amount);
        touch();
    }

    public void adjustStock(BigDecimal newQuantity) {
        if (newQuantity == null || newQuantity.signum() < 0) {
            throw new IllegalArgumentException(
                    "Adjusted quantity cannot be negative");
        }
        this.quantity = newQuantity;
        touch();
    }

    public void reserve(BigDecimal amount) {
        requirePositive(amount, "Amount to reserve");
        if (this.quantity.subtract(this.reserved).compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient available stock: available="
                            + this.quantity.subtract(this.reserved)
                                    .toPlainString()
                            + ", requested=" + amount.toPlainString());
        }
        this.reserved = this.reserved.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void releaseReserved(BigDecimal amount) {
        requirePositive(amount, "Amount to release");
        if (this.reserved.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Cannot release more than reserved: reserved="
                            + this.reserved.toPlainString() + ", requested="
                            + amount.toPlainString());
        }
        this.reserved = this.reserved.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void deductReserved(BigDecimal amount) {
        requirePositive(amount, "Amount to deduct");
        if (this.reserved.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Cannot deduct more than reserved: reserved="
                            + this.reserved.toPlainString() + ", requested="
                            + amount.toPlainString());
        }
        if (this.quantity.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient stock: available="
                    + this.quantity.toPlainString() + ", requested="
                    + amount.toPlainString());
        }
        this.quantity = this.quantity.subtract(amount);
        this.reserved = this.reserved.subtract(amount);
        touch();
    }

    private static void requirePositive(BigDecimal amount, String label) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(label + " must be positive");
        }
    }

    private void touch() {
        this.lastMovementAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAvailableQuantity() {
        return this.quantity.subtract(this.reserved);
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
    public BigDecimal getQuantity() {
        return quantity;
    }
    public BigDecimal getReserved() {
        return reserved;
    }
    public LocalDateTime getLastMovementAt() {
        return lastMovementAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
