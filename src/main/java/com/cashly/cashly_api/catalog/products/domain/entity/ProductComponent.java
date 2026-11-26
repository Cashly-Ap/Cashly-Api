package com.cashly.cashly_api.catalog.products.domain.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ProductComponent {

    private static final BigDecimal MINIMUM_QUANTITY = new BigDecimal("0.0001");

    private final UUID id;
    private final UUID parentProductId;
    private final UUID componentProductId;
    private final BigDecimal quantity;

    public ProductComponent(UUID id, UUID parentProductId,
            UUID componentProductId, BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Component quantity cannot be null");
        if (quantity.compareTo(MINIMUM_QUANTITY) < 0) {
            throw new IllegalArgumentException(
                    "Component quantity must be greater than zero");
        }
        Objects.requireNonNull(componentProductId,
                "Component product ID cannot be null");
        this.id = id;
        this.parentProductId = parentProductId;
        this.componentProductId = componentProductId;
        this.quantity = quantity;
    }

    public ProductComponent(UUID id, UUID parentProductId,
            UUID componentProductId, int quantity) {
        this(id, parentProductId, componentProductId,
                BigDecimal.valueOf(quantity));
    }

    public UUID getId() {
        return id;
    }
    public UUID getParentProductId() {
        return parentProductId;
    }
    public UUID getComponentProductId() {
        return componentProductId;
    }
    public BigDecimal getQuantity() {
        return quantity;
    }
}
