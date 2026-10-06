package com.cashly.cashly_api.quotes.domain.entities;

import com.cashly.cashly_api.quotes.domain.exceptions.InvalidQuoteLineException;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteAllocationId;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteLineId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuoteLine {

    private final QuoteLineId id;
    private final UUID productId;
    private final String productName;
    private final String productCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private final List<QuoteAllocation> allocations;

    public QuoteLine(UUID productId, String productName, String productCode, BigDecimal quantity, BigDecimal unitPrice) {
        if (productId == null) {
            throw new InvalidQuoteLineException("Product id cannot be null");
        }
        if (productName == null) {
            throw new InvalidQuoteLineException("Product name cannot be null");
        }
        if (productCode == null) {
            throw new InvalidQuoteLineException("Product code cannot be null");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuoteLineException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuoteLineException("Unit price must be greater than zero");
        }
        this.id = QuoteLineId.generate();
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity.multiply(unitPrice);
        this.allocations = new ArrayList<>();
    }

    private QuoteLine(QuoteLineId id, UUID productId, String productName, String productCode,
                      BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal,
                      List<QuoteAllocation> allocations) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.allocations = allocations != null ? new ArrayList<>(allocations) : new ArrayList<>();
    }

    public static QuoteLine reconstitute(QuoteLineId id, UUID productId, String productName, String productCode,
                                          BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal,
                                          List<QuoteAllocation> allocations) {
        return new QuoteLine(id, productId, productName, productCode, quantity, unitPrice, lineTotal, allocations);
    }

    public void updateQuantity(BigDecimal newQuantity) {
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuoteLineException("Quantity must be greater than zero");
        }
        this.quantity = newQuantity;
        this.lineTotal = this.quantity.multiply(this.unitPrice);
    }

    public void updateUnitPrice(BigDecimal newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuoteLineException("Unit price must be greater than zero");
        }
        this.unitPrice = newUnitPrice;
        this.lineTotal = this.quantity.multiply(this.unitPrice);
    }

    public void addAllocation(QuoteAllocation allocation) {
        allocations.add(allocation);
    }

    public void removeAllocation(QuoteAllocationId allocationId) {
        allocations.removeIf(a -> a.getId().equals(allocationId));
    }

    public QuoteLineId getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductCode() { return productCode; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public List<QuoteAllocation> getAllocations() { return Collections.unmodifiableList(allocations); }
}
