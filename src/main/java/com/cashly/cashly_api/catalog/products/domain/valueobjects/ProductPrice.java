package com.cashly.cashly_api.catalog.products.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ProductPrice {

    private final UUID id;
    private final UUID priceTypeId;
    private final BigDecimal price;
    private final BigDecimal profitMargin;

    private ProductPrice(UUID id, UUID priceTypeId, BigDecimal price, BigDecimal profitMargin) {
        Objects.requireNonNull(priceTypeId, "Price type ID cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.id = id;
        this.priceTypeId = priceTypeId;
        this.price = price;
        this.profitMargin = profitMargin;
    }

    public static ProductPrice create(UUID priceTypeId, BigDecimal price, BigDecimal profitMargin) {
        return new ProductPrice(null, priceTypeId, price, profitMargin);
    }

    public static ProductPrice reconstitute(UUID id, UUID priceTypeId, BigDecimal price, BigDecimal profitMargin) {
        return new ProductPrice(id, priceTypeId, price, profitMargin);
    }

    public UUID getId() { return id; }
    public UUID getPriceTypeId() { return priceTypeId; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getProfitMargin() { return profitMargin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductPrice that = (ProductPrice) o;
        return Objects.equals(priceTypeId, that.priceTypeId);
    }

    @Override
    public int hashCode() { return Objects.hashCode(priceTypeId); }
}
