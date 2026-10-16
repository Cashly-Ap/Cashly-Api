package com.cashly.cashly_api.products.infrastructure.persistence.entities;

import com.cashly.cashly_api.products.domain.entity.ProductComponent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "product_components")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductComponentEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "component_product_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID componentProductId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public static ProductComponentEntity fromDomain(ProductComponent component, ProductEntity parent) {
        ProductComponentEntity entity = new ProductComponentEntity();
        entity.id = component.getId();
        entity.product = parent;
        entity.componentProductId = component.getComponentProductId();
        entity.quantity = component.getQuantity();
        return entity;
    }

    public ProductComponent toDomain() {
        return new ProductComponent(id, product.getId(), componentProductId, quantity);
    }
}
