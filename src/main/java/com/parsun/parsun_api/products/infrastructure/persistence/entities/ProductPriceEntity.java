package com.parsun.parsun_api.products.infrastructure.persistence.entities;

import com.parsun.parsun_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import com.parsun.parsun_api.products.domain.valueobjects.ProductPrice;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_type_id", nullable = false)
    private PriceTypeEntity priceType;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    public static ProductPriceEntity fromDomain(ProductPrice productPrice, ProductEntity product,
                                                 PriceTypeEntity priceType) {
        ProductPriceEntity entity = new ProductPriceEntity();
        entity.id = productPrice.getId() != null ? productPrice.getId() : UUID.randomUUID();
        entity.product = product;
        entity.priceType = priceType;
        entity.price = productPrice.getPrice();
        return entity;
    }

    public ProductPrice toDomain() {
        return ProductPrice.reconstitute(id, priceType.getId(), price);
    }
}
