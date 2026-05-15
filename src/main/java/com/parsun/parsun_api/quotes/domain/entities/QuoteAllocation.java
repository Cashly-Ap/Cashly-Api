package com.parsun.parsun_api.quotes.domain.entities;

import com.parsun.parsun_api.quotes.domain.exceptions.InvalidQuoteAllocationException;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteAllocationId;

import java.math.BigDecimal;
import java.util.UUID;

public class QuoteAllocation {

    private final QuoteAllocationId id;
    private final UUID productId;
    private final UUID warehouseId;
    private final String warehouseName;
    private BigDecimal allocatedQuantity;

    public QuoteAllocation(UUID productId, UUID warehouseId, String warehouseName, BigDecimal allocatedQuantity) {
        if (productId == null) {
            throw new InvalidQuoteAllocationException("Product id cannot be null");
        }
        if (warehouseId == null) {
            throw new InvalidQuoteAllocationException("Warehouse id cannot be null");
        }
        if (warehouseName == null) {
            throw new InvalidQuoteAllocationException("Warehouse name cannot be null");
        }
        if (allocatedQuantity == null || allocatedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuoteAllocationException("Allocated quantity must be greater than zero");
        }
        this.id = QuoteAllocationId.generate();
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.allocatedQuantity = allocatedQuantity;
    }

    private QuoteAllocation(QuoteAllocationId id, UUID productId, UUID warehouseId, String warehouseName, BigDecimal allocatedQuantity) {
        this.id = id;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.allocatedQuantity = allocatedQuantity;
    }

    public static QuoteAllocation reconstitute(QuoteAllocationId id, UUID productId, UUID warehouseId, String warehouseName, BigDecimal allocatedQuantity) {
        return new QuoteAllocation(id, productId, warehouseId, warehouseName, allocatedQuantity);
    }

    public void updateAllocatedQuantity(BigDecimal newQuantity) {
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuoteAllocationException("Allocated quantity must be greater than zero");
        }
        this.allocatedQuantity = newQuantity;
    }

    public QuoteAllocationId getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getWarehouseId() { return warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public BigDecimal getAllocatedQuantity() { return allocatedQuantity; }
}
