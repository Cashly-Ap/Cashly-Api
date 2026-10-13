package com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.entities;

import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_prices")
@Getter
@Setter
@NoArgsConstructor
public class ProductPriceEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "price_type_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID priceTypeId;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "profit_margin")
    private BigDecimal profitMargin;

    public static ProductPriceEntity fromDomain(ProductPrice productPrice, ProductEntity product) {
        ProductPriceEntity entity = new ProductPriceEntity();
        entity.id = productPrice.getId() != null ? productPrice.getId() : UUID.randomUUID();
        entity.product = product;
        entity.priceTypeId = productPrice.getPriceTypeId();
        entity.price = productPrice.getPrice();
        entity.profitMargin = productPrice.getProfitMargin();
        return entity;
    }

    public ProductPrice toDomain() {
        return ProductPrice.reconstitute(id, priceTypeId, price, profitMargin);
    }
}
