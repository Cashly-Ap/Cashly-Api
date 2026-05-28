package com.parsun.parsun_api.returns.domain.entities;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReturnLine {

    private final UUID productId;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    private final List<ReturnLineAllocation> allocations;

    private ReturnLine(UUID productId, BigDecimal quantity, BigDecimal unitPrice,
                       BigDecimal subtotal, List<ReturnLineAllocation> allocations) {
        Objects.requireNonNull(productId, "Product id cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        Objects.requireNonNull(subtotal, "Subtotal cannot be null");

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be greater than or equal to zero");
        }

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.allocations = allocations != null ? List.copyOf(allocations) : List.of();
    }

    public static ReturnLine create(UUID productId, BigDecimal quantity, BigDecimal unitPrice,
                                    BigDecimal subtotal, List<ReturnLineAllocation> allocations) {
        return new ReturnLine(productId, quantity, unitPrice, subtotal, allocations);
    }

    public UUID getProductId() { return productId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public List<ReturnLineAllocation> getAllocations() { return allocations; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnLine that = (ReturnLine) o;
        return Objects.equals(productId, that.productId)
                && Objects.equals(quantity, that.quantity)
                && Objects.equals(unitPrice, that.unitPrice)
                && Objects.equals(subtotal, that.subtotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice, subtotal);
    }

    @Override
    public String toString() {
        return "ReturnLine{productId=" + productId + ", quantity=" + quantity + "}";
    }
}
