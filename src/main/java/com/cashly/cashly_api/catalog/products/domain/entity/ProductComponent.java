package com.cashly.cashly_api.catalog.products.domain.entity;

import java.util.Objects;
import java.util.UUID;

public class ProductComponent {

    private final UUID id;
    private final UUID parentProductId;
    private final UUID componentProductId;
    private final int quantity;

    public ProductComponent(UUID id, UUID parentProductId,
            UUID componentProductId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Component quantity must be at least 1");
        }
        Objects.requireNonNull(componentProductId,
                "Component product ID cannot be null");
        this.id = id;
        this.parentProductId = parentProductId;
        this.componentProductId = componentProductId;
        this.quantity = quantity;
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
    public int getQuantity() {
        return quantity;
    }
}
