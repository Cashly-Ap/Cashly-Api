package com.cashly.cashly_api.purchases.domain;

import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseLineId;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PurchaseLine {

    private final PurchaseLineId id;
    private final UUID productId;
    private final String productName;
    private final String productCode;
    private final BigDecimal quantity;
    private final BigDecimal unitCost;
    private final BigDecimal lineTotal;
    private final List<PurchaseLineAllocation> allocations;

    private PurchaseLine(PurchaseLineId id, UUID productId, String productName, String productCode,
                         BigDecimal quantity, BigDecimal unitCost, List<PurchaseLineAllocation> allocations) {
        Objects.requireNonNull(id, "PurchaseLine id cannot be null");
        Objects.requireNonNull(productId, "Product id cannot be null");
        Objects.requireNonNull(productName, "Product name cannot be null");
        Objects.requireNonNull(productCode, "Product code cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitCost, "Unit cost cannot be null");

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit cost must be greater than or equal to zero");
        }

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.lineTotal = quantity.multiply(unitCost);
        this.allocations = allocations != null ? List.copyOf(allocations) : List.of();
    }

    public static PurchaseLine create(UUID productId, String productName, String productCode,
                                       BigDecimal quantity, BigDecimal unitCost,
                                       List<PurchaseLineAllocation> allocations) {
        return new PurchaseLine(PurchaseLineId.generate(), productId, productName, productCode,
                quantity, unitCost, allocations);
    }

    public static PurchaseLine reconstitute(PurchaseLineId id, UUID productId, String productName,
                                             String productCode, BigDecimal quantity,
                                             BigDecimal unitCost, BigDecimal lineTotal,
                                             List<PurchaseLineAllocation> allocations) {
        return new PurchaseLine(id, productId, productName, productCode, quantity, unitCost, allocations);
    }

    public PurchaseLineId getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductCode() { return productCode; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public List<PurchaseLineAllocation> getAllocations() { return allocations; }
}
