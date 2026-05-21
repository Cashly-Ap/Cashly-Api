package com.parsun.parsun_api.finances.domain.entities;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ReturnLine {

    private final UUID id;
    private final UUID returnId;
    private final UUID invoiceLineId;
    private final UUID productId;
    private final BigDecimal quantity;
    private final UUID warehouseId;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    private ReturnLine(UUID id, UUID returnId, UUID invoiceLineId, UUID productId,
                       BigDecimal quantity, UUID warehouseId, BigDecimal unitPrice, BigDecimal lineTotal) {
        Objects.requireNonNull(returnId, "Return id cannot be null");
        Objects.requireNonNull(invoiceLineId, "Invoice line id cannot be null");
        Objects.requireNonNull(productId, "Product id cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        Objects.requireNonNull(warehouseId, "Warehouse id cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.returnId = returnId;
        this.invoiceLineId = invoiceLineId;
        this.productId = productId;
        this.quantity = quantity;
        this.warehouseId = warehouseId;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity.multiply(unitPrice);
    }

    public static ReturnLine create(UUID returnId, UUID invoiceLineId, UUID productId,
                                     BigDecimal quantity, UUID warehouseId, BigDecimal unitPrice) {
        return new ReturnLine(null, returnId, invoiceLineId, productId, quantity, warehouseId, unitPrice, null);
    }

    public static ReturnLine reconstitute(UUID id, UUID returnId, UUID invoiceLineId, UUID productId,
                                           BigDecimal quantity, UUID warehouseId, BigDecimal unitPrice) {
        BigDecimal lineTotal = quantity.multiply(unitPrice);
        return new ReturnLine(id, returnId, invoiceLineId, productId, quantity, warehouseId, unitPrice, lineTotal);
    }

    public UUID getId() { return id; }
    public UUID getReturnId() { return returnId; }
    public UUID getInvoiceLineId() { return invoiceLineId; }
    public UUID getProductId() { return productId; }
    public BigDecimal getQuantity() { return quantity; }
    public UUID getWarehouseId() { return warehouseId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
