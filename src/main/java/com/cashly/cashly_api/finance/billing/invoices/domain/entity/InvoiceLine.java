package com.cashly.cashly_api.finance.billing.invoices.domain.entity;

import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.ReturnQuantityExceedsException;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceLineId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InvoiceLine {

    private final InvoiceLineId id;
    private final UUID productId;
    private final String productName;
    private final String productCode;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;
    private BigDecimal returnedQuantity;
    private final List<InvoiceLineAllocation> allocations;

    private InvoiceLine(InvoiceLineId id, UUID productId, String productName, String productCode,
                        BigDecimal quantity, BigDecimal unitPrice,
                        BigDecimal returnedQuantity, List<InvoiceLineAllocation> allocations) {
        Objects.requireNonNull(id, "InvoiceLine id cannot be null");
        Objects.requireNonNull(productId, "Product id cannot be null");
        Objects.requireNonNull(productName, "Product name cannot be null");
        Objects.requireNonNull(productCode, "Product code cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be greater than or equal to zero");
        }

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity.multiply(unitPrice);
        this.returnedQuantity = returnedQuantity != null ? returnedQuantity : BigDecimal.ZERO;
        this.allocations = allocations != null ? List.copyOf(allocations) : List.of();
    }

    public static InvoiceLine create(UUID productId, String productName, String productCode,
                                      BigDecimal quantity, BigDecimal unitPrice,
                                      List<InvoiceLineAllocation> allocations) {
        return new InvoiceLine(InvoiceLineId.generate(), productId, productName, productCode,
                quantity, unitPrice, BigDecimal.ZERO, allocations);
    }

    public static InvoiceLine reconstitute(InvoiceLineId id, UUID productId, String productName,
                                           String productCode, BigDecimal quantity,
                                           BigDecimal unitPrice, BigDecimal returnedQuantity,
                                           List<InvoiceLineAllocation> allocations) {
        return new InvoiceLine(id, productId, productName, productCode, quantity, unitPrice,
                returnedQuantity, allocations);
    }

    public InvoiceLineId getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductCode() { return productCode; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public BigDecimal getReturnedQuantity() { return returnedQuantity; }
    public List<InvoiceLineAllocation> getAllocations() { return allocations; }

    public void accumulateReturnedQuantity(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        BigDecimal newReturnedQuantity = this.returnedQuantity.add(quantity);
        if (newReturnedQuantity.compareTo(this.quantity) > 0) {
            throw new ReturnQuantityExceedsException(this.id.value(), newReturnedQuantity, this.quantity);
        }
        this.returnedQuantity = newReturnedQuantity;
    }

    public void decreaseReturnedQuantity(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        BigDecimal newReturnedQuantity = this.returnedQuantity.subtract(quantity);
        if (newReturnedQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Returned quantity cannot be negative");
        }
        this.returnedQuantity = newReturnedQuantity;
    }
}
