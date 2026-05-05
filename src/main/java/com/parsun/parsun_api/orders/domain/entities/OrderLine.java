package com.parsun.parsun_api.orders.domain.entities;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.parsun.parsun_api.orders.domain.valueobjects.OrderLineId;

public class OrderLine {

    private final OrderLineId id;
    private final UUID productId;
    private final String productName;
    private final String productCode;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    private OrderLine(OrderLineId id, UUID productId, String productName, String productCode,
                      BigDecimal quantity, BigDecimal unitPrice) {
        Objects.requireNonNull(id, "OrderLine id cannot be null");
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
    }

    public static OrderLine create(UUID productId, String productName, String productCode,
                                   BigDecimal quantity, BigDecimal unitPrice) {
        return new OrderLine(OrderLineId.generate(), productId, productName, productCode,
                quantity, unitPrice);
    }

    public static OrderLine reconstitute(OrderLineId id, UUID productId, String productName,
                                         String productCode, BigDecimal quantity,
                                         BigDecimal unitPrice) {
        return new OrderLine(id, productId, productName, productCode, quantity, unitPrice);
    }

    public OrderLineId getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductCode() { return productCode; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
